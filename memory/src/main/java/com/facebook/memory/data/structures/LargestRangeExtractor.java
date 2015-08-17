package com.facebook.memory.data.structures;

import java.util.TreeSet;

import com.facebook.collections.heaps.Heap;
import com.facebook.collections.heaps.IntRange;

public class LargestRangeExtractor implements RangeExtractor {
  @Override
  public IntRange extract(int size, Heap<IntRange> rangeSetBySize, TreeSet<IntRange> rangeSetByStart) {
    IntRange range = rangeSetBySize.pollLast();

    rangeSetByStart.remove(range);

    return range;
  }
}
