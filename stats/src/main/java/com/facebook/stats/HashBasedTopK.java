package com.facebook.stats;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Longs;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/*
 * Hash-based implementation of streaming top-k for a generic key type.
 * This version is optimized for fast update of the counts.
 * Time complexity of add() is O(1) on average.
 * Time complexity of getTopK() is O(n log k).
 * Space usage is O(n).
 * (n = keySpaceSize)
 */
public class HashBasedTopK<T extends Comparable<T>> implements TopK<T> {
  private final int k;
  private final Map<T, Long> counts;

  public HashBasedTopK(int k) {
    this.k = k;
    // k is a decent guess to start with
    counts = new HashMap<T, Long>(k);
  }

  @Override
  public synchronized void add(T key, long count) {
    Preconditions.checkNotNull(key, "key can't be null");
    Preconditions.checkArgument(count >= 0, "count to add must be non-negative, got %s", count);

    Long currentCount = counts.get(key);

    if (currentCount == null) {
      currentCount = 0L;
    }

    counts.put(key, currentCount + count);
  }

  @Override
  public synchronized List<T> getTopK() {
    Comparator<T> comparator = new Comparator<T>() {
      public int compare(T key1, T key2) {
        return Longs.compare(counts.get(key1), counts.get(key2));
      }
    };
    PriorityQueue<T> topK = new PriorityQueue<T>(k, comparator);

    for (Map.Entry<T, Long> entry : counts.entrySet()) {
      if (topK.size() < k) {
        topK.offer(entry.getKey());
      } else if (entry.getValue() > counts.get(topK.peek())) {
        topK.offer(entry.getKey());
        topK.poll();
      }
    }

    LinkedList<T> sortedTopK = new LinkedList<T>();

    while (!topK.isEmpty()) {
      sortedTopK.addFirst(topK.poll());
    }

    return sortedTopK;
  }
}
