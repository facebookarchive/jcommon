package com.facebook.memory.data.structures.freelists;

import javax.annotation.Nullable;
import java.util.TreeSet;

import com.facebook.collections.orderedset.OrderedSet;
import com.facebook.collections.orderedset.IntRange;

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
  IntRange extract(int size, OrderedSet<IntRange> rangeSetBySize, TreeSet<IntRange> rangeSetByStart);
}
