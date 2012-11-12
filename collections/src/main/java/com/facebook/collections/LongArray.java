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

import com.google.common.base.Preconditions;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static com.google.common.base.Preconditions.checkState;

/**
 * Reference implementation of SimpleArray using longs. Attempts to limit memory growth
 * by allowing a small growth factor each resize
 *
 */
@ThreadSafe
public class LongArray implements Array<Long> {
  private static final int DEFAULT_INITIAL_CAPACITY = 3;
  // memory conservative growth amount here
  private static final double MIN_GROWTH_FACTOR = 0.3;
  private static final int MIN_GROWTH_AMOUNT = 8; // intel xeon cache line size is 64 bytes
  private static final long EMPTY = -1;

  private long[] data;
  private volatile int nextWritePosition = 0;
  private volatile int size = 0;

  public LongArray(int initialCapacity) {
    data = new long[initialCapacity];
    Arrays.fill(data, EMPTY);
  }

  public LongArray() {
    this(DEFAULT_INITIAL_CAPACITY);
  }

  @Override
  public synchronized Long get(int i) throws IndexOutOfBoundsException {
    if (i >= size()) {
      throw new ArrayIndexOutOfBoundsException();
    }
    // return null if the slot is empty
    return data[i] >= 0 ? data[i] : null;
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public synchronized int capacity() {
    return data.length;
  }

  @Override
  public Long set(int i, Long value) throws ArrayIndexOutOfBoundsException {
    if (value == null) {
      throw new NullPointerException("null values not allowed");
    }

    synchronized (this) {
      if (i >= data.length) {
        throw new ArrayIndexOutOfBoundsException(
          String.format("tried to set value at index %d, but  max index is %d", i, data.length - 1)
        );
      }

      Long oldValue = data[i];

      data[i] = value;

      if (oldValue == EMPTY) {
        size++;
      }

      return oldValue;
    }
  }

  @Override
  public synchronized int append(Long value) {
    int myWritePosition;

    if (nextWritePosition >= data.length) {
      int sizeIncrease = (int) (data.length * MIN_GROWTH_FACTOR);

      sizeIncrease = Math.max(sizeIncrease, MIN_GROWTH_AMOUNT);

      internalResize(data.length + sizeIncrease);
    }

    // find an empty slot if the current one isn't (may have been set by set(int i, Long value)
    while (!isEmpty(nextWritePosition)) {
      nextWritePosition++;

      if (nextWritePosition >= data.length - 1) {
        // resize in advance for next write
        resize(data.length + MIN_GROWTH_AMOUNT);
      }
    }

    checkState(isEmpty(nextWritePosition) && nextWritePosition < data.length);

    myWritePosition = nextWritePosition;
    // prepare for next write
    nextWritePosition++;
    size++;
    data[myWritePosition] = value;

    return myWritePosition;
  }

  @Override
  public synchronized Long remove(int i) throws ArrayIndexOutOfBoundsException {
    if (isEmpty(i)) {
      return null;
    } else {
      Long previousValue = data[i];

      data[i] = EMPTY;
      size--;

      return convertValue(previousValue);
    }
  }

  @Override
  public int resize(int sizeHint) {
    return internalResize(sizeHint);
  }

  /**
   * @param sizeHint effectively the new size (NOT the increase). May be changed for memory or
   *                 cache alignments as implementations see fit
   * @return actual new size
   */
  private synchronized int internalResize(int sizeHint) {
    Preconditions.checkArgument(sizeHint > 0, "sizeHint must be > 0");

    // we can use the sizeHint directly. Cases we wouldn't would be to align the array on
    // word boundaries and not waste space (ex: int arrays always even sized, etc. We could even
    // use cache line boundaries to maximize the chance of cache write hits (believed 64 bytes
    // on our Xeon L5520 processors "cache_alignment : 64")
    int newSize = sizeHint;

    long[] newData = new long[newSize];

    System.arraycopy(data, 0, newData, 0, data.length);
    Arrays.fill(newData, data.length, newData.length, EMPTY);
    data = newData;

    return newSize;
  }

  /**
   * @return
   */
  @Override
  public Iterator<Long> iterator() {
    return new Iter();
  }

  /**
   * since the contract to clients is that null => empty slot, we convert our empty indicators
   * to null here
   *
   * @param value input from data
   * @return input if >= 0, else null
   */
  private Long convertValue(long value) {
    return value >= 0 ? value : null;
  }

  private boolean isEmpty(int position) {
    return data[position] < 0;
  }

  /**
   * Thread safe iterator. Note: at the moment the iterator is created, sizeSnapshot elements exist
   * in the array. It will terminate upon seeing the first sizeSnapshot elements, or when position
   * is >= capacitySnapshot. This means fewer elements or different elements may be seen, but
   * so goes the life of concurrent data structure access
   * <p/>
   * It also supports remove, but again, note that this may not have the intended effect if the
   * underlying array is resized. Prefer directly removing an element with
   * {@link #LongSimpleArray.remove(int i)}
   */
  private class Iter implements Iterator<Long> {
    // index to iterate through the array.
    private int position = 0;
    // -2 is no read called yet, -1 means remove was calledon this position
    private int lastReadPosition = -2;
    private int numSeen = 0;
    private long sizeSnapshot = size;
    private int capacitySnapshot = data.length;
    private Long nextValue = null;
    // indicates if we've read the next value
    private boolean readNextValue = false;
    // indicates if last read attemp found a value
    private boolean hasNextValue = false;

    @Override
    public boolean hasNext() {
      synchronized (LongArray.this) {
        if (readNextValue) {
          return hasNextValue;
        }

        boolean retVal = numSeen < sizeSnapshot && position < capacitySnapshot;

        // this loop finds the next non-null value, or due to concurrent changes, it may simply
        // terminate if it iterates over the entire capacity of the array w/o finding sizeSnapshot
        // elements
        while (retVal) {
          nextValue = convertValue(data[position]);

          if (nextValue != null) {
            break;
          }

          position++;
          // since deletions could occur, hitting the end of capacity before the size is possible
          retVal = numSeen < sizeSnapshot && position < capacitySnapshot;
        }

        if (retVal) {
          lastReadPosition = position;
        }

        readNextValue = true;
        hasNextValue = retVal;

        return retVal;
      }
    }

    @Override
    public Long next() {
      synchronized (LongArray.this) {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }

        readNextValue = false;
        position++;

        return nextValue;
      }
    }

    @Override
    public void remove() {
      synchronized (LongArray.this) {
        if (lastReadPosition == -2) {
          throw new IllegalStateException("next() has not been called yet");
        }
        if (lastReadPosition == -1) {
          throw new IllegalStateException("remove already called for this position");
        }

        data[lastReadPosition] = EMPTY;
        size--;
        lastReadPosition = -1;
      }
    }
  }
}
