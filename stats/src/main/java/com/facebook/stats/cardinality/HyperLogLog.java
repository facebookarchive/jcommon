package com.facebook.stats.cardinality;

import com.google.common.base.Preconditions;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * An implementation of the HyperLogLog algorithm:
 * <p/>
 * http://algo.inria.fr/flajolet/Publications/FlFuGaMe07.pdf
 */
@NotThreadSafe
public class HyperLogLog {
  private static final HashFunction HASH = Hashing.murmur3_128();
  private final byte[] buckets;

  // The current sum of 1 / (1L << buckets[i]). Updated as new items are added and used for
  // estimation
  private double currentSum;
  private int nonZeroBuckets = 0;

  public HyperLogLog(int numberOfBuckets) {
    Preconditions.checkArgument(
      Numbers.isPowerOf2(numberOfBuckets),
      "numberOfBuckets must be a power of 2"
    );
    Preconditions.checkArgument(numberOfBuckets > 0, "numberOfBuckets must be > 0");

    buckets = new byte[numberOfBuckets];
    currentSum = buckets.length;
  }

  public HyperLogLog(int[] buckets) {
    this(buckets.length);

    currentSum = 0;
    for (int i = 0; i < buckets.length; i++) {
      int value = buckets[i];
      Preconditions.checkArgument(
        value >= 0 && value <= Byte.MAX_VALUE,
        "values must be > 0 and <= %s, found %s",
        Byte.MAX_VALUE,
        value
      );
      this.buckets[i] = (byte) value;
      currentSum += 1.0 / (1 << value);
    }
  }

  public void add(long value) {
    long hash = HASH.hashLong(value).asLong();

    int bucketMask = buckets.length - 1;
    int bucket = (int) (hash & bucketMask);

    // set the bits used for the bucket index to 1 to avoid them affecting the count of leading
    // zeros (very unlikely, but...)
    int highestBit = Long.numberOfLeadingZeros(hash | bucketMask) + 1;

    int previous = buckets[bucket];

    if (previous == 0) {
      nonZeroBuckets++;
    }

    if (highestBit > previous) {
      currentSum -= 1.0 / (1L << previous);
      currentSum += 1.0 / (1L << highestBit);

      buckets[bucket] = (byte) highestBit;
    }
  }

  public long estimate() {
    double alpha;
    switch (buckets.length) {
      case (1 << 4):
        alpha = 0.673;
        break;
      case (1 << 5):
        alpha = 0.697;
        break;
      case (1 << 6):
        alpha = 0.709;
        break;
      default:
        alpha = (0.7213 / (1 + 1.079 / buckets.length));
    }

    double result = alpha * buckets.length * buckets.length / currentSum;

    if (result <= 2.5 * buckets.length) {
      // adjust for small cardinalities
      int zeroBuckets = buckets.length - nonZeroBuckets;
      if (zeroBuckets > 0) {
        result = buckets.length * Math.log(buckets.length * 1.0 / zeroBuckets);
      }
    }

    return Math.round(result);
  }

  public int[] buckets() {
    int[] result = new int[buckets.length];

    for (int i = 0; i < buckets.length; i++) {
      result[i] = buckets[i];
    }

    return result;
  }
}
