package com.facebook.memory.data.structures;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.facebook.memory.FailedAllocationException;
import com.facebook.memory.slabs.Span;

public class TreeSetFreeList implements FreeList {
  private final TreeSet<IntRange> freeRanges;
  private final TreeSet<IntRange> freeRangeBySize;

  public TreeSetFreeList(int size) {
    IntRange range = Ranges.make(0, size - 1);

    freeRanges = Sets.newTreeSet();
    freeRanges.add(range);
    freeRangeBySize = new TreeSet<>(
      (o1, o2) -> {
        int sigNum = Integer.signum(o1.getSize() - o2.getSize());

        // break size ties by location
        if (sigNum == 0) {
          return Integer.signum(o1.getLower() - o2.getLower());
        } else {
          return sigNum;
        }
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
  public synchronized Span tryAllocate(int size) {
    if (freeRangeBySize.isEmpty()) {
      return Span.emptySpan();
    } else {
      IntRange range = freeRangeBySize.pollLast();
      int actualAllocation = Math.min(size, range.getSize());

      allocateFromRange(actualAllocation, range);

      return new Span(range.getLower(), actualAllocation);
    }
  }

  @Override
  public synchronized int allocate(int size) throws FailedAllocationException {
    if (freeRanges.isEmpty()) {
      throw new FailedAllocationException(
        String.format("unable to find free segment of size %d. Memory exhausted", size)
      );
    }


    IntRange range = freeRangeBySize.pollLast();

    if (range.getSize() >= size) {
      allocateFromRange(size, range);

      return range.getLower();
    } else {
      freeRangeBySize.add(range);
      throw new FailedAllocationException(
        String.format("unable to find free segment of size %d. Max of %d", size, range.getSize())
      );
    }
  }

  private void allocateFromRange(int size, IntRange range) {
    freeRanges.remove(range);

    if (range.getSize() > size) {
      IntRange shavedRange = range.shave(size);

      freeRanges.add(shavedRange);
      freeRangeBySize.add(shavedRange);
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
  public synchronized int getSize() {
    return freeRangeBySize.stream().mapToInt(IntRange::getSize).sum();
  }

  @Override
  public String toString() {
    return "TreeSetFreeList{" +
      "freeRanges=" + freeRanges +
      ", freeRangeBySize=" + freeRangeBySize +
      '}';
  }
}
