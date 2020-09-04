/*
 * Copyright (C) 2012 Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.collections;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;

/**
 * Set of Longs that is memory optimized for data sets containing mostly contiguous blocks of longs.
 * This collection is NOT thread-safe.
 */
public class RangeSet extends AbstractSet<Long> implements Set<Long> {
  /** Map that indexes each LongSegment with the smallest value in its range */
  private final NavigableMap<Long, LongSegment> map = new TreeMap<>();

  private int size = 0;

  @Override
  public int size() {
    return size;
  }

  @Override
  public boolean contains(Object o) {
    Long value = ((Number) o).longValue();
    Map.Entry<Long, LongSegment> entry = map.floorEntry(value);
    return entry != null && entry.getValue().contains(value);
  }

  /**
   * Iterator returns the longs in increasing order
   *
   * @return iterator
   */
  @Override
  public Iterator<Long> iterator() {
    return new Iterator<Long>() {
      // Get Segments in ascending order
      private final Iterator<LongSegment> segmentIterator = map.values().iterator();
      private Iterator<Long> longIterator =
          segmentIterator.hasNext() ? segmentIterator.next().iterator() : null;

      @Override
      public boolean hasNext() {
        return longIterator != null && longIterator.hasNext();
      }

      @Override
      public Long next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }
        Long ret = longIterator.next();
        if (!longIterator.hasNext()) {
          longIterator = segmentIterator.hasNext() ? segmentIterator.next().iterator() : null;
        }
        return ret;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException("Iterator does not support remove");
      }
    };
  }

  @Override
  public boolean add(Long aLong) {
    // Find the largest LongSegment with a min value of less than or equal to aLong
    Map.Entry<Long, LongSegment> lowerEntry = map.floorEntry(aLong);

    // This value is already added
    if (lowerEntry != null && lowerEntry.getValue().contains(aLong)) {
      return false;
    }

    // Get the LongSegment with a min value directly after aLong
    LongSegment upperSegment = map.get(aLong + 1);

    // Determine possibly adjacencies with lower and upper bound LongSegments
    boolean lowerAdjacent = lowerEntry != null && lowerEntry.getValue().getMax() + 1 == aLong;
    boolean upperAdjacent = upperSegment != null && upperSegment.getMin() - 1 == aLong;

    if (lowerAdjacent && upperAdjacent) {

      // Overwrite the lower adjacent to encompass the whole range
      map.put(
          lowerEntry.getValue().getMin(),
          new LongSegment(lowerEntry.getValue().getMin(), upperSegment.getMax()));

      // Remove the upper adjacent b/c now included in the merged LongSegment
      map.remove(upperSegment.getMin());

    } else if (lowerAdjacent) {

      // Overwrite the lower adjacent max to include aLong
      map.put(
          lowerEntry.getValue().getMin(), new LongSegment(lowerEntry.getValue().getMin(), aLong));

    } else if (upperAdjacent) {

      // Insert new LongSegment starting aLong and encompassing upper adjacent
      map.put(aLong, new LongSegment(aLong, upperSegment.getMax()));
      // Remove the upper adjacent b/c now included in new LongSegment
      map.remove(upperSegment.getMin());

    } else {

      // No adjacents, so just insert singular element
      map.put(aLong, new LongSegment(aLong));
    }

    size++;
    return true;
  }

  @Override
  public boolean remove(Object o) {
    Long value = ((Number) o).longValue();
    Map.Entry<Long, LongSegment> entry = map.floorEntry(value);

    // Entry does not exist
    if (entry == null) {
      return false;
    }

    // Entry does not contain the value
    if (!entry.getValue().contains(value)) {
      return false;
    }

    // Update lower segment if necessary
    if (entry.getValue().getMin() < value) {
      map.put(entry.getValue().getMin(), new LongSegment(entry.getValue().getMin(), value - 1));
    } else {
      map.remove(entry.getValue().getMin()); // Otherwise, remove existing segment/index
    }

    // Make an upper segment if necessary
    if (entry.getValue().getMax() > value) {
      map.put(value + 1, new LongSegment(value + 1, entry.getValue().getMax()));
    }

    size--;
    return true;
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    boolean changed = false;
    for (Object o : c) {
      changed |= remove(o);
    }
    return changed;
  }

  @Override
  public void clear() {
    map.clear();
    size = 0;
  }

  /** Represents a range of long values from min to max (inclusive) */
  private static class LongSegment implements Iterable<Long> {
    private final long min;
    private final long max;

    private LongSegment(long min, long max) {
      this.min = min;
      this.max = max;
      if (max < min) {
        throw new IllegalArgumentException();
      }
    }

    private LongSegment(long value) {
      this(value, value);
    }

    public long getMin() {
      return min;
    }

    public long getMax() {
      return max;
    }

    public boolean contains(long value) {
      return value >= min && value <= max;
    }

    @Override
    public Iterator<Long> iterator() {
      return new Iterator<Long>() {
        private long currentValue = min;

        @Override
        public boolean hasNext() {
          return currentValue <= max;
        }

        @Override
        public Long next() {
          if (!hasNext()) {
            throw new NoSuchElementException();
          }
          long ret = currentValue;
          currentValue++;
          return ret;
        }

        @Override
        public void remove() {
          throw new UnsupportedOperationException("Iterator does not support remove");
        }
      };
    }
  }
}
