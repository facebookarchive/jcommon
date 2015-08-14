package com.facebook.memory.data.structures.freelists;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import com.facebook.collections.heaps.HeapPartition;
import com.facebook.collections.heaps.HeapPartitions;
import com.facebook.collections.heaps.IntRange;
import com.facebook.collections.heaps.NavigableSetFactory;
import com.facebook.memory.data.structures.BestFitRangeExtractor;
import com.facebook.memory.data.structures.LargestRangeExtractor;

public class FreeLists {
  private static final int MIN_PARTITION_SIZE_DEFAULT = 1024;
  private static final NavigableSetFactory<IntRange> INT_RANGE_SET_FACTORY =
    () -> new TreeSet<>(IntRange.getSizeComparator());

  public static FreeList bestFitFreeList(int size) {
    return new TreeSetFreeList(size, new BestFitRangeExtractor());
  }

  public static FreeList largestRangeFreeList(int size) {
    return new TreeSetFreeList(size, new LargestRangeExtractor());
  }

  public static HeapPartition<IntRange>[] exponentialParitions(int size) {
    return exponentialParitions(MIN_PARTITION_SIZE_DEFAULT, size);
  }

  /**
   * creates paritions of size 2^n up to a given totalSize, with a catch-all after that
   * @param minSize
   * @param totalSize
   * @return
   */
  public static HeapPartition<IntRange>[] exponentialParitions(int minSize, int totalSize) {
    List<IntRange> intRangeList = new ArrayList<>();
    int lastEnd = 0;

    for (int i = minSize; i < totalSize; i = 2 * i) {
      intRangeList.add(IntRange.make(lastEnd, i));
      lastEnd = i;
    }

    // add last partition that allows
    intRangeList.add(IntRange.make(lastEnd, Integer.MAX_VALUE));

    IntRange[] intRanges = intRangeList.toArray(new IntRange[intRangeList.size()]);
    HeapPartition<IntRange>[] heapPartitions = HeapPartitions.createHeapParitions(intRanges, INT_RANGE_SET_FACTORY);

    return heapPartitions;
  }

  public static HeapPartition<IntRange>[] equalSizedParitions(int partitionSize, int totalSize) {
    throw new UnsupportedOperationException();
  }

}
