package com.facebook.stats;

import com.facebook.collections.ComparablePair;
import com.google.common.base.Preconditions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/*
 * Tree-based implementation of streaming top-k.
 * This version is optimized for fast retrieval of the top k elements.
 * Time complexity of add() is O(log k) worst case, O(1) best case.
 * Time complexity of getTopK() is O(k).
 * Space usage is O(n + k).
 * (n = keySpaceSize)
 */
public class TreeBasedTopK<T extends Comparable<T>> implements TopK<T> {
  private final int k;
  private final Map<T, Long> counts = new HashMap<T, Long>();
  private final Set<T> topKeys;
  private final TreeSet<ComparablePair<Long, T>> topPairs = new TreeSet<ComparablePair<Long, T>>();
  private long smallestTopCount = Long.MAX_VALUE;

  public TreeBasedTopK(int k) {
    this.k = k;
    topKeys = new HashSet<T>(k);
  }

  /*
   * Cost of an add() operation is O(log k) if the candidate has to be inserted in
   * the current top-k elements, O(1) otherwise.
   */
  @Override
  public synchronized void add(T key, long count) {
    Preconditions.checkNotNull(key, "key can't be null");
    Preconditions.checkArgument(count >= 0, "count to add must be non-negative, got %s", count);

    if (count == 0) {
      return;
    }

    Long currentCount = counts.get(key);

    if (currentCount == null) {
      currentCount = 0L;
    }

    long updatedCount = currentCount + count;

    counts.put(key, updatedCount);

    if (topKeys.contains(key)) {
      topPairs.remove(new ComparablePair<Long, T>(currentCount, key));
      topPairs.add(new ComparablePair<Long, T>(updatedCount, key));
    } else if (topPairs.size() < k) {
      topPairs.add(new ComparablePair<Long, T>(updatedCount, key));
      topKeys.add(key);
      smallestTopCount = Math.min(smallestTopCount, updatedCount);
    } else if (updatedCount > smallestTopCount) {
      ComparablePair<Long, T> smallestTopPair = topPairs.pollFirst();

      topKeys.remove(smallestTopPair.getSecond());
      topPairs.add(new ComparablePair<Long, T>(updatedCount, key));
      topKeys.add(key);
      smallestTopCount = topPairs.first().getFirst();
    }
  }

  @Override
  public synchronized List<T> getTopK() {
    LinkedList<T> topK = new LinkedList<T>();

    for (ComparablePair<Long, T> pair : topPairs) {
      topK.addFirst(pair.getSecond());
    }

    return topK;
  }
}
