package com.facebook.collections.heaps;

import java.util.Arrays;
import java.util.NavigableSet;

public class HeapPartitions {
  public static <T extends Comparable<?super T>> HeapPartition<T>[] createHeapParitions(
    T[] boundaries, NavigableSetFactory<T> setFactory
  ) {
    Arrays.sort(boundaries);

    HeapPartition<T>[] heapPartitions = new HeapPartition[boundaries.length];

    for (int i = 0; i < heapPartitions.length; i++) {
      NavigableSet<T> set = setFactory.create();
      HeapPartition<T> heapPartition = new HeapPartition<>(set, boundaries[i]);

      heapPartitions[i] = heapPartition;
    }

    return heapPartitions;
  }
}
