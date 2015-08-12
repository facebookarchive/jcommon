package com.facebook.memory.data.structures.freelists;

import java.util.TreeSet;

import com.facebook.collections.orderedset.OrderedSet;
import com.facebook.collections.orderedset.IntRange;

public class LargestRangeExtractor implements RangeExtractor {
  @Override
  public IntRange extract(int size, OrderedSet<IntRange> rangeSetBySize, TreeSet<IntRange> rangeSetByStart) {
    IntRange range = rangeSetBySize.pollLast();

    rangeSetByStart.remove(range);

    return range;
  }
}
