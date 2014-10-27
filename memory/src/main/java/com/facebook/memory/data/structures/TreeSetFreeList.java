package com.facebook.memory.data.structures;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.facebook.memory.FailedAllocationException;
import com.facebook.memory.MemoryConstants;

public class TreeSetFreeList implements FreeList {
  private final long baseAddress;
  private final TreeSet<IntRange> freeRanges;

  public TreeSetFreeList(long baseAddress, int size) {
    this.baseAddress = baseAddress;
    freeRanges = Sets.newTreeSet();
    freeRanges.add(Ranges.make(0, size - 1));
  }

  @Override
  public synchronized void extend(int size) {
    IntRange last = freeRanges.last();

    freeRanges.remove(last);
    freeRanges.add(last.extend(size));
  }

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
    freeRanges.add(range);
  }

  @Override
  public synchronized long allocate(int size) throws FailedAllocationException {
    if (freeRanges.isEmpty()) {
      throw new FailedAllocationException(
        String.format("unable to find free segment of size %d. Memory exhausted", size)
      );
    }

    Iterator<IntRange> iter = freeRanges.iterator();

    int maxSize = Integer.MIN_VALUE;

    while (iter.hasNext()) {
      IntRange range = iter.next();

      maxSize = Math.max(maxSize, range.size());

      if (range.size() >= size) {
        iter.remove();
        freeRanges.remove(range);

        if (range.size() > size) {
          freeRanges.add(range.shave(size));
        }

        return baseAddress + range.getLower();
      }
    }

    throw new FailedAllocationException(
      String.format("unable to find free segment of size %d. Max of %d", size, maxSize)
    );
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
      "baseAddress=" + baseAddress +
      ", freeRanges=" + freeRanges +
      '}';
  }
}
