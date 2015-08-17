package com.facebook.memory.data.structures;

import javax.annotation.Nullable;
import java.util.TreeSet;

import com.facebook.collections.heaps.Heap;
import com.facebook.collections.heaps.IntRange;

public interface RangeExtractor {
  /**
   * assumes the set is sorted by size in ascending order
   *
   * note: removes the range from the set
   *
   *
   * @param size
   * @param rangeSetBySize
   * @param rangeSetByStart
   * @return
   */
  @Nullable
  IntRange extract(int size, Heap<IntRange> rangeSetBySize, TreeSet<IntRange> rangeSetByStart);
}
