package com.facebook.memory.data.structures;

import java.util.TreeSet;

import com.facebook.collections.heaps.Heap;
import com.facebook.collections.heaps.IntRange;
import com.facebook.memory.data.structures.freelists.Ranges;

public class BestFitRangeExtractor implements RangeExtractor {
  @Override
  public IntRange extract(int size, Heap<IntRange> rangeSetBySize, TreeSet<IntRange> rangeSetByStart) {
    IntRange range = rangeSetBySize.ceiling(Ranges.make(1, size));
    if (range != null) {
      rangeSetBySize.remove(range);
      rangeSetByStart.remove(range);
    }

    return range;
  }
}
