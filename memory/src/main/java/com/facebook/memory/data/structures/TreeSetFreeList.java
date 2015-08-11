package com.facebook.memory.data.structures;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.facebook.memory.FailedAllocationException;

public class TreeSetFreeList implements FreeList {
  private final TreeSet<IntRange> freeRanges;
  private final TreeSet<IntRange> freeRangeBySize;

  public TreeSetFreeList(int size) {
    IntRange range = Ranges.make(0, size - 1);

    freeRanges = Sets.newTreeSet();
    freeRanges.add(range);
    freeRangeBySize = new TreeSet<>(
      (o1, o2) -> {
        return Integer.signum(o1.size() - o2.size());
      }
    );

    freeRangeBySize.add(range);
  }

  @Override
  public synchronized void extend(int size) {
    IntRange last = freeRanges.last();

    freeRanges.remove(last);

    IntRange extendedRange = last.extend(size);

    freeRanges.add(extendedRange);
    freeRangeBySize.add(extendedRange);
  }

  @SuppressWarnings("NumericCastThatLosesPrecision")
  @Override
  public synchronized void free(int offset, int size) {
    IntRange range = Ranges.make(offset, offset + size - 1);
    IntRange lowerRange = freeRanges.lower(range);
    IntRange higherRange = freeRanges.higher(range);
    List<IntRange> toRemove = Lists.newArrayList();

    if (range.isAdjacentTo(lowerRange)) {
      range = range.span(lowerRange);
      toRemove.add(lowerRange);
    }

    if (range.isAdjacentTo(higherRange)) {
      range = range.span(higherRange);
      toRemove.add(higherRange);
    }

    freeRanges.removeAll(toRemove);
    freeRangeBySize.removeAll(toRemove);
    freeRanges.add(range);
    freeRangeBySize.add(range);
  }

  @Override
  public synchronized int allocate(int size) throws FailedAllocationException {
    if (freeRanges.isEmpty()) {
      throw new FailedAllocationException(
        String.format("unable to find free segment of size %d. Memory exhausted", size)
      );
    }


    IntRange range = freeRangeBySize.pollLast();

    if (range.size() >= size) {
      freeRanges.remove(range);

      if (range.size() > size) {
        IntRange shavedRange = range.shave(size);

        freeRanges.add(shavedRange);
        freeRangeBySize.add(shavedRange);
      }

      return range.getLower();
    } else {
      freeRangeBySize.add(range);
      throw new FailedAllocationException(
        String.format("unable to find free segment of size %d. Max of %d", size, range.size())
      );
    }
  }

  @Override
  public Set<IntRange> asRangeSet() {
    return Collections.unmodifiableSet(freeRanges);
  }

  @Override
  public synchronized void reset(int size) {
    freeRanges.clear();

    if (size > 0) {
      freeRanges.add(Ranges.make(0, size - 1));
    }
  }

  @Override
  public String toString() {
    return "TreeSetFreeList{" +
      "freeRanges=" + freeRanges +
      ", freeRangeBySize=" + freeRangeBySize +
      '}';
  }
}
