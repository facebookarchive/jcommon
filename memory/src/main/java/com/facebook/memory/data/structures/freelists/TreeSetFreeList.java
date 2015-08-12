package com.facebook.memory.data.structures.freelists;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.facebook.collections.orderedset.OrderedSet;
import com.facebook.collections.orderedset.IntRange;
import com.facebook.collections.orderedset.PartitionedOrderedSet;
import com.facebook.memory.FailedAllocationException;
import com.facebook.memory.slabs.Span;

public class TreeSetFreeList implements FreeList {
  private static final int MIN_RANGE_SIZE_DEFAULT = 1024;
  // sorted by start ascending
  private final TreeSet<IntRange> freeRangeByStart;
  // sorted by size ascending
  private final OrderedSet<IntRange> freeRangeBySize;
  private final RangeExtractor rangeExtractor;
  private volatile int currentSize;

  public TreeSetFreeList(int size, int minPartitionSize, RangeExtractor rangeExtractor) {
    this.rangeExtractor = rangeExtractor;
    currentSize = size;

    IntRange range = Ranges.make(0, size - 1);

    freeRangeByStart = Sets.newTreeSet();
    freeRangeByStart.add(range);
    freeRangeBySize = new PartitionedOrderedSet<>(
      FreeLists.exponentialParitions(minPartitionSize, size), IntRange.getSizeComparator()
    );
    freeRangeBySize.add(range);
  }

  public TreeSetFreeList(int size, int minPartitionSize) {
    this(size, minPartitionSize, new LargestRangeExtractor());
  }

  public TreeSetFreeList(int size, RangeExtractor rangeExtractor) {
    this(size, MIN_RANGE_SIZE_DEFAULT, rangeExtractor);
  }

  public TreeSetFreeList(int size) {
    this(size, MIN_RANGE_SIZE_DEFAULT);
  }

  @Override
  public synchronized void extend(int size) {
    IntRange last = freeRangeByStart.pollLast();

    freeRangeBySize.remove(last);

    IntRange extendedRange = last.extend(size);

    freeRangeByStart.add(extendedRange);
    freeRangeBySize.add(extendedRange);
    currentSize += size;
  }

  @SuppressWarnings("NumericCastThatLosesPrecision")
  @Override
  public synchronized void free(int offset, int size) {
    IntRange range = Ranges.make(offset, offset + size - 1);
    IntRange lowerRange = freeRangeByStart.lower(range);
    IntRange higherRange = freeRangeByStart.higher(range);
    List<IntRange> toRemove = Lists.newArrayList();
    Preconditions.checkState(!range.overlaps(lowerRange), "range %s overlaps with existing %s", range, lowerRange);
    Preconditions.checkState(
      !range.overlaps(higherRange),
      "range [%s] overlaps with existing [%s]",
      range,
      higherRange
    );

    if (range.isAdjacentTo(lowerRange)) {
      range = range.span(lowerRange);
      toRemove.add(lowerRange);
    }

    if (range.isAdjacentTo(higherRange)) {
      range = range.span(higherRange);
      toRemove.add(higherRange);
    }

    freeRangeByStart.removeAll(toRemove);
    freeRangeBySize.removeAll(toRemove);
    freeRangeByStart.add(range);
    freeRangeBySize.add(range);
    currentSize += size;
  }

  @Override
  public synchronized Span tryAllocate(int size) {
    if (freeRangeBySize.isEmpty()) {
      return Span.emptySpan();
    } else {
      IntRange range = rangeExtractor.extract(size, freeRangeBySize, freeRangeByStart);

      if (range == null) {
        return Span.emptySpan();
      } else {
        int actualAllocation = Math.min(range.getSize(), size);

        allocateFromRange(actualAllocation, range);

        return new Span(range.getLower(), actualAllocation);
      }
    }
  }

  @Override
  public synchronized int allocate(int size) throws FailedAllocationException {
    if (freeRangeByStart.isEmpty()) {
      throw new FailedAllocationException(
        String.format("unable to find free segment of size %d. Memory exhausted", size)
      );
    }
    //
    IntRange range = rangeExtractor.extract(size, freeRangeBySize, freeRangeByStart);

    if (range != null && allocateFromRange(size, range)) {

      return range.getLower();
    }

    throw new FailedAllocationException(
      String.format("unable to find free segment of size %d. Max of %d", size, range == null ? 0 : range.getSize())
    );
  }

  @Override
  public Set<IntRange> asRangeSet() {
    return Collections.unmodifiableSet(freeRangeByStart);
  }

  @Override
  public synchronized void reset(int size) {
    freeRangeByStart.clear();

    if (size > 0) {
      freeRangeByStart.add(Ranges.make(0, size - 1));
      currentSize = size;
    }
  }

  @Override
  public synchronized int getSize() {
    return currentSize;
  }

  @Override
  public String toString() {
    return "TreeSetFreeList{" +
      "freeRanges=" + freeRangeByStart +
      ", freeRangeBySize=" + freeRangeBySize +
      '}';
  }

  private boolean allocateFromRange(int size, IntRange range) {
    if (range.getSize() >= size) {
      IntRange shavedRange = range.shave(size);

      if (shavedRange.getSize() > 0) {
        freeRangeByStart.add(shavedRange);
        freeRangeBySize.add(shavedRange);
      }

      currentSize -= size;

      return true;
    } else {
      freeRangeBySize.add(range);
      freeRangeByStart.add(range);

      return false;
    }
  }
}
