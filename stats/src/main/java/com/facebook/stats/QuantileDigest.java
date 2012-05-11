package com.facebook.stats;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.Ordering;
import com.google.common.collect.PeekingIterator;
import com.google.common.util.concurrent.AtomicDouble;

import javax.annotation.concurrent.ThreadSafe;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.lang.String.format;

/**
 * <p></p>Implements http://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.132.7343, a data
 * structure for approximating quantiles by trading off error with memory requirements.</p>
 *
 * <p></p>The size of the digest is adjusted dynamically to achieve the error bound and requires
 * O(log2(U) / maxError) space, where <em>U</em> is the number of bits needed to represent the
 * domain of the values added to the digest.</p>
 *
 * <p>The error is defined as the discrepancy between the real rank of the value returned in a
 * quantile query and the rank corresponding to the queried quantile.</p>
 *
 * <p>Thus, for a query for quantile <em>q</em> that returns value <em>v</em>, the error is
 * |rank(v) - q * N| / N, where N is the number of elements added to the digest and rank(v) is the
 * real rank of <em>v</em></p>
 *
 * <p>This class also supports exponential decay. The implementation is based on the ideas laid out
 * in http://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.159.3978</p>
 */
@ThreadSafe
public class QuantileDigest {
  private static final int MAX_BITS = 64;
  private static final double MAX_SIZE_FACTOR = 1.5;

  // needs to be such that Math.exp(alpha * seconds) does not grow too big
  static final long RESCALE_THRESHOLD_SECONDS = 50;
  private static final double ZERO_WEIGHT_THRESHOLD = 1e-5;

  private final double maxError;
  private final Clock clock;
  private final double alpha;
  private final boolean compressAutomatically;

  private Node root;

  private double weightedCount;
  private long max;

  private long landmarkInSeconds;

  private int totalNodeCount = 0;
  private int nonZeroNodeCount = 0;
  private int compressions = 0;
  private int maxTotalNodeCount = 0;
  private int maxTotalNodesAfterCompress = 0;

  /**
   * <p>Create a QuantileDigest with a maximum error guarantee of "maxError" and no decay.
   *
   * @param maxError the max error tolerance
   */
  public QuantileDigest(double maxError) {
    this(maxError, 0);
  }

  /**
   *<p>Create a QuantileDigest with a maximum error guarantee of "maxError" and exponential decay
   * with factor "alpha".</p>
   *
   * @param maxError the max error tolerance
   * @param alpha the exponential decay factor
   */
  public QuantileDigest(double maxError, double alpha) {
    this(maxError, alpha, new RealtimeClock(), true);
  }

  @VisibleForTesting
  QuantileDigest(double maxError, double alpha, Clock clock, boolean compressAutomatically) {
    checkArgument(maxError >= 0 && maxError <= 1, "maxError must be in range [0, 1]");
    checkArgument(alpha >= 0 && alpha < 1, "alpha must be in range [0, 1)");

    this.maxError = maxError;
    this.alpha = alpha;
    this.clock = clock;
    this.compressAutomatically = compressAutomatically;

    landmarkInSeconds = TimeUnit.MILLISECONDS.toSeconds(clock.getMillis());
  }

  /**
   * Adds a value to this digest. The value must be >= 0
   */
  public synchronized void add(long value) {
    checkArgument(value >= 0, "value must be >= 0");

    rescaleIfNeeded();

    double weight = weight(TimeUnit.MILLISECONDS.toSeconds(clock.getMillis()));
    weightedCount += weight;

    max = Math.max(max, value);
    root = insertAt(root, value, weight);

    // The size (number of non-zero nodes) of the digest is at most 3 * compression factor
    // If we're over MAX_SIZE_FACTOR of the expected size, compress
    // Note: we don't compress as soon as we go over expectedNodeCount to avoid unnecessarily
    // running a compression for every new added element when we're close to boundary
    int maxExpectedNodeCount = 3 * calculateCompressionFactor();
    if (nonZeroNodeCount > MAX_SIZE_FACTOR * maxExpectedNodeCount && compressAutomatically) {
      compress();
    }
  }

  /**
   * Gets the values at the specified quantiles +/- maxError. The list of quantiles must be sorted
   * in increasing order, and each value must be in the range [0, 1]
   */
  public synchronized List<Long> getQuantiles(List<Double> quantiles) {
    checkArgument(Ordering.natural().isOrdered(quantiles),
                  "quantiles must be sorted in increasing order");
    for (double quantile : quantiles) {
      checkArgument(quantile >= 0 && quantile <= 1, "quantile must be between [0,1]");
    }

    final ImmutableList.Builder<Long> builder = ImmutableList.builder();
    final PeekingIterator<Double> iterator = Iterators.peekingIterator(quantiles.iterator());

    postOrderTraversal(root, new Callback() {
      private double sum = 0;

      public boolean process(Node node) {
        sum += node.weightedCount;

        while (iterator.hasNext() && sum > iterator.peek() * weightedCount) {
          iterator.next();

          // we know the max value ever seen, so cap the percentile to provide better error
          // bounds in this case
          long value = Math.min(node.getUpperBound(), max);

          builder.add(value);
        }

        return iterator.hasNext();
      }
    });

    // we finished the traversal without consuming all quantiles. This means the remaining quantiles
    // correspond to the max known value
    while (iterator.hasNext()) {
      builder.add(max);
      iterator.next();
    }

    return builder.build();
  }

  /**
   * Gets the value at the specified quantile +/- maxError. The quantile must be in the range [0, 1]
   */
  public synchronized long getQuantile(double quantile) {
    return getQuantiles(ImmutableList.of(quantile)).get(0);
  }

  /**
   * Number (decayed) of elements added to this quantile digest
   */
  public synchronized double getCount() {
    return weightedCount / weight(TimeUnit.MILLISECONDS.toSeconds(clock.getMillis()));
  }

  /*
   * Get the exponentially-decayed approximate counts of values in multiple buckets. The elements in
   * the provided list denote the upper bound each of the buckets and must be sorted in ascending
   * order.
   *
   * The approximate count in each bucket is guaranteed to be within 2 * totalCount * maxError of
   * the real count.
   */
  public synchronized List<Double> getHistogram(List<Long> bucketUpperBounds) {
    checkArgument(
      Ordering.natural().isOrdered(bucketUpperBounds),
      "buckets must be sorted in increasing order"
    );

    final ImmutableList.Builder<Double> builder = ImmutableList.builder();
    final PeekingIterator<Long> iterator = Iterators.peekingIterator(bucketUpperBounds.iterator());

    final AtomicDouble sum = new AtomicDouble();
    final AtomicDouble lastSum = new AtomicDouble();
    final double normalizationFactor = weight(TimeUnit.MILLISECONDS.toSeconds(clock.getMillis()));

    postOrderTraversal(root, new Callback() {
      public boolean process(Node node) {

        while (iterator.hasNext() && iterator.peek() <= node.getUpperBound()) {
          builder.add((sum.get() - lastSum.get()) / normalizationFactor);
          lastSum.set(sum.get());
          iterator.next();
        }

        sum.addAndGet(node.weightedCount);
        return iterator.hasNext();
      }
    });

    while (iterator.hasNext()) {
      builder.add((sum.get() - lastSum.get()) / normalizationFactor);
      iterator.next();
    }

    return builder.build();
  }

  @VisibleForTesting
  synchronized int getTotalNodeCount() {
    return totalNodeCount;
  }

  @VisibleForTesting
  synchronized int getNonZeroNodeCount() {
    return nonZeroNodeCount;
  }

  @VisibleForTesting
  synchronized int getCompressions() {
    return compressions;
  }

  @VisibleForTesting
  synchronized void compress() {
    ++compressions;

    final int compressionFactor = calculateCompressionFactor();

    postOrderTraversal(root, new Callback() {
      public boolean process(Node node) {
        if (node.isLeaf()) {
          return true;
        }

        double sum = node.weightedCount;
        if (node.left != null) {
          sum += node.left.weightedCount;
        }
        if (node.right != null) {
          sum += node.right.weightedCount;
        }

        if (sum < weightedCount / compressionFactor) {
          node.left = tryRemove(node.left);
          node.right = tryRemove(node.right);

          if (node.weightedCount < ZERO_WEIGHT_THRESHOLD && sum > ZERO_WEIGHT_THRESHOLD) {
            ++nonZeroNodeCount;
          }

          node.weightedCount = sum;
        }

        return true;
      }
    });

    maxTotalNodesAfterCompress = Math.max(maxTotalNodesAfterCompress, totalNodeCount);
  }

  private double weight(long timestamp) {
    return Math.exp(alpha * (timestamp - landmarkInSeconds));
  }

  private void rescaleIfNeeded() {
    // rescale the weights based on a new landmark to avoid numerical overflow issues

    long nowInSeconds = TimeUnit.MILLISECONDS.toSeconds(clock.getMillis());

    if (nowInSeconds - landmarkInSeconds > RESCALE_THRESHOLD_SECONDS) {
      final double factor = Math.exp(-alpha * (nowInSeconds - landmarkInSeconds));

      weightedCount *= factor;

      postOrderTraversal(root, new Callback() {
          public boolean process(Node node) {
            node.weightedCount *= factor;
            return true;
          }
      });

      landmarkInSeconds = nowInSeconds;
    }
  }

  private int calculateCompressionFactor() {
    return Math.max((int) ((root.level + 1) / maxError), 1);
  }

  private Node insertAt(Node node, long value, double weight) {
    if (node == null) {
      ++totalNodeCount;
      maxTotalNodeCount = Math.max(maxTotalNodeCount, totalNodeCount);
      ++nonZeroNodeCount;
      return new Node(value, 0, weight);
    }

    if (node.level == 0 && node.value == value) {
      checkState(node.weightedCount > ZERO_WEIGHT_THRESHOLD,
        "Expected node count of leaf node to be > %d", ZERO_WEIGHT_THRESHOLD);

      node.weightedCount += weight;
      return node;
    }

    // the mask for the prefix of a node at a given level
    long pathPrefixMask = (0x7FFFFFFFFFFFFFFFL << node.level) & 0x7FFFFFFFFFFFFFFFL;

    if ((value & pathPrefixMask) != (node.value & pathPrefixMask)) {
      // if value and node.value are not in the same branch given node's level,
      // insert a parent above them at the point at which branches diverge
      int parentLevel = MAX_BITS - Long.numberOfLeadingZeros(node.value ^ value);

      // the mask for the prefix of the node at the level at which "value" and the current
      // node branch out
      long commonPrefixMask = (0x7FFFFFFFFFFFFFFFL << parentLevel) & 0x7FFFFFFFFFFFFFFFL;
      Node newParent = new Node(value & commonPrefixMask, parentLevel, 0);
      Node newNode = new Node(value, 0, weight);

      // the branch is given by the bit at the level one below parent
      long newNodeBranch = value & (1L << (parentLevel - 1));
      if (newNodeBranch == 0) {
        newParent.left = newNode;
        newParent.right = node;
      } else {
        newParent.left = node;
        newParent.right = newNode;
      }

      totalNodeCount += 2;
      maxTotalNodeCount = Math.max(maxTotalNodeCount, totalNodeCount);

      ++nonZeroNodeCount;
      return newParent;
    }

    // we're on the right branch of the tree and we haven't reached a leaf,
    // so keep going down
    long branch = value & (1L << (node.level - 1));

    if (branch == 0) {
      node.left = insertAt(node.left, value, weight);
    } else {
      node.right = insertAt(node.right, value, weight);
    }

    return node;
  }

  /**
   * Remove the node if possible or set its count to 0 if it has children and
   * it needs to be kept around
   */
  private Node tryRemove(Node node) {
    if (node == null) {
      return null;
    }

    if (node.weightedCount > ZERO_WEIGHT_THRESHOLD) {
      --nonZeroNodeCount;
    }

    Node result = null;
    if (node.isLeaf()) {
      --totalNodeCount;
    }
    else if (node.hasSingleChild()) {
      result = node.getSingleChild();
      --totalNodeCount;
    }
    else {
      node.weightedCount = 0;
      result = node;
    }

    return result;
  }

  // returns true if traversal should continue
  private boolean postOrderTraversal(Node node, Callback callback) {
    if (node.left != null && !postOrderTraversal(node.left, callback)) {
      return false;
    }

    if (node.right != null && !postOrderTraversal(node.right, callback)) {
      return false;
    }

    return callback.process(node);
  }

  /**
   * Computes the maximum error of the current digest
   */
  public synchronized double getConfidenceFactor() {
    return computeMaxPathWeight(root) * 1.0 / weightedCount;
  }

  /**
   * Computes the max "weight" of any path starting at node and ending at a leaf in the
   * hypothetical complete tree. The weight is the sum of counts in the ancestors of a given node
   */
  private double computeMaxPathWeight(Node node) {
    if (node == null || node.level == 0) {
      return 0;
    }

    double leftMaxWeight = computeMaxPathWeight(node.left);
    double rightMaxWeight = computeMaxPathWeight(node.right);

    return Math.max(leftMaxWeight, rightMaxWeight) + node.weightedCount;
  }

  @VisibleForTesting
  synchronized void validate() {
    final AtomicDouble sumOfWeights = new AtomicDouble();
    final AtomicInteger actualNodeCount = new AtomicInteger();
    final AtomicInteger actualNonZeroNodeCount = new AtomicInteger();

    if (root != null) {
      validateStructure(root);

      postOrderTraversal(root, new Callback() {
        @Override
        public boolean process(Node node) {
          sumOfWeights.addAndGet(node.weightedCount);
          actualNodeCount.incrementAndGet();

          if (node.weightedCount > ZERO_WEIGHT_THRESHOLD) {
            actualNonZeroNodeCount.incrementAndGet();
          }

          return true;
        }
      });
    }

    checkState(sumOfWeights.get() == weightedCount,
               "Computed weight (%s) doesn't match summary (%s)", sumOfWeights.get(),
               weightedCount);

    checkState(actualNodeCount.get() == totalNodeCount,
      "Actual node count (%s) doesn't match summary (%s)",
      actualNodeCount.get(), totalNodeCount);

    checkState(actualNonZeroNodeCount.get() == nonZeroNodeCount,
      "Actual non-zero node count (%s) doesn't match summary (%s)",
      actualNonZeroNodeCount.get(), nonZeroNodeCount);
  }

  private void validateStructure(Node node) {
    checkState(node.level >= 0);

    if (node.left != null) {
      validateBranchStructure(node, node.left, node.right, true);
      validateStructure(node.left);
    }

    if (node.right != null) {
      validateBranchStructure(node, node.right, node.left, false);
      validateStructure(node.right);
    }
  }

  private void validateBranchStructure(Node parent, Node child, Node otherChild, boolean isLeft) {
    checkState(child.level < parent.level,
               "Child level (%s) should be smaller than parent level (%s)", child.level,
               parent.level);

    long branch = child.value & (1L << (parent.level - 1));
    checkState(branch == 0 && isLeft || branch != 0 && !isLeft,
               "Value of child node is inconsistent with its branch");

    Preconditions.checkState(parent.weightedCount >= ZERO_WEIGHT_THRESHOLD ||
                               child.weightedCount >= ZERO_WEIGHT_THRESHOLD || otherChild != null,
                             "Found a linear chain of zero-weight nodes");
  }


  private static class Node {
    private double weightedCount;
    private int level;
    private long value;
    private Node left;
    private Node right;

    private Node(long value, int level, double weightedCount) {
      this.value = value;
      this.level = level;
      this.weightedCount = weightedCount;
    }

    public boolean isLeaf() {
      return left == null && right == null;
    }

    public boolean hasSingleChild() {
      return left == null && right != null || left != null && right == null;
    }

    public Node getSingleChild() {
      checkState(hasSingleChild(), "Node does not have a single child");
      return Objects.firstNonNull(left, right);
    }

    public long getUpperBound() {
      // set all lsb below level to 1 (we're looking for the highest value of the range covered
      // by this node)
      long mask = (1L << level) - 1;
      return value | mask;
    }

    public String toString() {
      return format("%s (level = %d, count = %s, left = %s, right = %s)", value, level,
                    weightedCount, left != null, right != null);
    }
  }

  private static interface Callback {
    /**
     * @param node the node to process
     * @return true if processing should continue
     */
    boolean process(Node node);
  }
}
