package com.facebook.collections.orderedset;

import java.util.Arrays;
import java.util.NavigableSet;

public class OrderedSetPartitions {
  public static <T extends Comparable<?super T>> OrderedSetPartition<T>[] createHeapParitions(
    T[] boundaries, NavigableSetFactory<T> setFactory
  ) {
    Arrays.sort(boundaries);

    OrderedSetPartition<T>[] orderedSetPartitions = new OrderedSetPartition[boundaries.length];

    for (int i = 0; i < orderedSetPartitions.length; i++) {
      NavigableSet<T> set = setFactory.create();
      OrderedSetPartition<T> orderedSetPartition = new OrderedSetPartition<>(set, boundaries[i]);

      orderedSetPartitions[i] = orderedSetPartition;
    }

    return orderedSetPartitions;
  }
}
