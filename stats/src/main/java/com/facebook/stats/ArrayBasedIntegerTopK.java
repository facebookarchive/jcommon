package com.facebook.stats;

import com.google.common.base.Preconditions;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

/*
 * Array-based implementation of integer key streaming top-k.
 * This version is optimized for fast update of the counts.
 * Time complexity of add() is O(1).
 * Time complexity of getTopK() is O(n log k).
 * Space usage is O(n).
 * (n = keySpaceSize)
 */
public class ArrayBasedIntegerTopK implements TopK<Integer> {
  private final int k;
  private final long[] counts;

  public ArrayBasedIntegerTopK(int keySpaceSize, int k) {
    this.k = k;
    counts = new long[keySpaceSize];
  }

  @Override
  public synchronized void add(Integer key, long count) {
    Preconditions.checkNotNull(key, "key can't be null");
    Preconditions.checkElementIndex(key, counts.length, "key");
    Preconditions.checkArgument(count >= 0, "count to add must be non-negative, got %s", count);

    counts[key] += count;
  }

  @Override
  public List<Integer> getTopK() {
    PriorityQueue<Integer> topK = new PriorityQueue<Integer>(
      k,
      new Comparator<Integer>() {
        public int compare(Integer i, Integer j) {
          return Long.signum(counts[i] - counts[j]);
        }
      });

    for (int key = 0; key < counts.length; ++key) {
      if (topK.size() < k) {
        if (counts[key] > 0) {
          topK.offer(key);
        }
      } else if (counts[key] > counts[topK.peek()]) {
        topK.poll();
        topK.offer(key);
      }
    }

    LinkedList<Integer> sortedTopK = new LinkedList<Integer>();
    while (topK.size() > 0) {
      sortedTopK.addFirst(topK.poll());
    }
    return sortedTopK;
  }
}
