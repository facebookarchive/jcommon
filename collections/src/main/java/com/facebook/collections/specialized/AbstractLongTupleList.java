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
package com.facebook.collections.specialized;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * List of long-tuples that are sorted by their first element in ascending order. The first element
 * may not be negative, but subsequent ones may be.
 *
 * <p>The list will have an initial amount of space allocated according to initialListSize, but will
 * grow/shrink as items are added and removed
 */
public abstract class AbstractLongTupleList implements LongTupleHeap {
  private static final int SORT_INDEX = 0;
  private static final int DEFAULT_INITIAL_LIST_SIZE = 1; // size in tuples
  private static final int DEFAULT_TUPLE_SIZE = 2;
  private static final int ALLOCATION_CHUNK_SIZE = 1; // size in tuples
  private static final int EMPTY = -1; // sentinel to indicate empty

  private long[] tuples;
  private volatile int size = 0; // in # of tuples

  /**
   * @param initialListSize initial number of tuples for which to allocate spacee
   * @param tupleSize
   */
  protected AbstractLongTupleList(int initialListSize, int tupleSize) {
    this.tuples = new long[initialListSize * tupleSize];
    Arrays.fill(tuples, EMPTY);
    setHeadIndex(0);
  }

  protected AbstractLongTupleList(long[] tuples, int size) {
    this.tuples = tuples;
    this.size = size;
  }

  public AbstractLongTupleList(AbstractLongTupleList otherTupleList) {
    // we want a consistent view of otherTupleList, so synchronize on it
    synchronized (otherTupleList) {
      if (getTupleSize() != otherTupleList.getTupleSize()) {
        throw new IllegalArgumentException(
            String.format(
                "mismatched tuple sizes: [%d] and [%d]",
                getTupleSize(), otherTupleList.getTupleSize()));
      }
      tuples = Arrays.copyOf(otherTupleList.tuples, otherTupleList.tuples.length);
      size = otherTupleList.size;
    }
  }

  /**
   * must not refer to 'this' at all; should return a constant, or value computed on other
   * well-formed objects
   *
   * @return
   */
  protected abstract int getTupleSize();

  /**
   * creates a new heap
   *
   * @return
   */
  protected abstract LongTupleHeap copyHeap(long[] tuples, int size);

  @Override
  public synchronized long[] peek() {
    return findSmallest(false);
  }

  @Override
  public synchronized long[] poll() {
    return findSmallest(true);
  }

  @Override
  public synchronized boolean add(long[] tuple) {
    if (tuple.length != getTupleSize()) {
      throw new IllegalArgumentException(
          String.format("tuples must be of size %d ", getTupleSize()));
    }

    if (tuple[SORT_INDEX] < 0) {
      throw new IllegalArgumentException(
          String.format("tuple[%d] with value %d is not >= 0 ", SORT_INDEX, tuple[SORT_INDEX]));
    }

    if (!spaceFor(1)) {
      resize();
    }

    // returns the location we should insert at
    int insertLocation = findInsertLocation(tuple[SORT_INDEX]);

    if (insertLocation >= tuples.length) {
      // means we need to insert at the end, but it's not empty
      int numShifted = getTupleSize() * leftCompact();

      insertLocation -= numShifted;
    } else if (!isEmpty(insertLocation)) {
      // we shift the tuples over by one at the insert location, and adjust our insert
      // location accordingly
      insertLocation = rightShift(insertLocation);
    } // else the location is empty already

    insertAt(tuple, insertLocation);
    updateHeadIndex(insertLocation);

    return true;
  }

  @Override
  public synchronized boolean addAll(Collection<? extends long[]> tuples) {
    throw new UnsupportedOperationException("not yet");
  }

  @Override
  public int size() {
    return size;
  }

  /** @return # of long elements saved */
  @Override
  public synchronized int shrink() {
    if (size == 0 || translate(size) == tuples.length) {
      return 0;
    }

    leftCompact();

    int minSize = Math.max(1, size);
    int rawSize = getTupleSize() * minSize;
    int saved = tuples.length - rawSize;
    long[] newTuples = new long[rawSize];

    if (size > 0) {
      System.arraycopy(tuples, 0, newTuples, 0, rawSize);
    } else {
      Arrays.fill(newTuples, EMPTY);
      // indicate the 'head' index is 0
      newTuples[0] = 0;
    }

    tuples = newTuples;

    return saved;
  }

  @Override
  public synchronized LongTupleHeap makeCopy() {
    return copyHeap(tuples, size);
  }

  @Override
  public Iterator<long[]> iterator() {
    return new Iter();
  }

  /**
   * moves all values left so there are no empty slots at the start
   *
   * @return # of empty slots we found t the start
   */
  private int leftCompact() {
    if (size == 0) {
      return 0;
    }
    // headIndex tells us the length of empty portion before the first non-empty location
    int headLength = getHeadIndex();

    if (headLength == 0) {
      return 0;
    }

    System.arraycopy(tuples, headLength, tuples, 0, translate(size));

    return headLength;
  }

  /**
   * moves all values left so there are no empty slots at the start. Works if there are empty gaps
   * between elements
   *
   * <p>keeping this around for potential future tweaks if we find a sparse array makes sense
   *
   * @return # of empty slots we found at the start of the list
   */
  private int leftCompactSparse() {
    int translatedWritePosition = 0;
    int translatedReadPosition = getTupleSize();
    int numProcessed = 0;
    int emptySlots = 1;

    while (numProcessed < size) {
      if (isEmpty(translatedWritePosition)) {
        while (isEmpty(translatedReadPosition) && translatedReadPosition < tuples.length) {
          translatedReadPosition += getTupleSize();
          emptySlots++;
        }

        if (translatedReadPosition >= tuples.length) {
          throw new IllegalStateException(
              "compact failed--couldn't find non-empty to copy to empty");
        }

        swap(translatedWritePosition, translatedReadPosition);
      }
      // invariant: after each iteration, the item at translatedWritePosition is  considered
      // processed
      numProcessed++;

      translatedWritePosition += getTupleSize();
      translatedReadPosition += getTupleSize();
    }

    return emptySlots;
  }

  private void swap(int firstTranslatedPosition, int secondTranslatedPosition) {
    long[] tmpList = new long[getTupleSize()];

    System.arraycopy(tuples, firstTranslatedPosition, tmpList, 0, getTupleSize());
    System.arraycopy(
        tuples, secondTranslatedPosition, tuples, firstTranslatedPosition, getTupleSize());
    System.arraycopy(tmpList, 0, tuples, secondTranslatedPosition, getTupleSize());
  }

  /**
   * finds the first slot that has a tuple >= value
   *
   * @return index of the slot, or the index to the left if it's empty
   */
  private int findInsertLocation(long value) {
    // negative value at 0th slot => start index of non-empty values
    int startIndex = getHeadIndex();
    int endIndex = startIndex + translate(size);

    int i;
    for (i = startIndex; i < endIndex; i += getTupleSize()) {
      if (startIndex >= getTupleSize() && isEmpty(i - getTupleSize()) && tuples[i] >= value) {
        return i - getTupleSize();
      }

      if (isEmpty(i) || tuples[i] >= value) {
        break;
      }
    }

    return i;
  }

  /**
   * invariant: there is an empty slot at the position returned. This method will call leftCompact()
   * if it needs to in order to make room for any right-shifts needed
   *
   * @param start
   */
  private int rightShift(int start) {
    if (!isEmpty(tuples.length - getTupleSize())) {
      int numShifted = getTupleSize() * leftCompact();

      start -= numShifted;
    }

    // last index of last tuple
    int endIndex = getHeadIndex() + translate(size) - 1;
    // right-shift by;  [start, endIndex] (inclusive)
    System.arraycopy(tuples, start, tuples, start + getTupleSize(), endIndex - start + 1);

    return start;
  }

  private boolean isEmpty(int position) {
    return tuples[position] < 0;
  }

  private boolean spaceFor(int numItems) {
    return translate(size - 1) + getTupleSize() * numItems < tuples.length;
  }

  /**
   * @param tuple tuple to insert
   * @param translatedPosition translated translatedPosition
   */
  private void insertAt(long[] tuple, int translatedPosition) {
    if (tuple.length != getTupleSize()) {
      throw new IllegalArgumentException(
          String.format("tuples must be of size %d ", getTupleSize()));
    }

    int i = 0;

    for (long item : tuple) {
      tuples[translatedPosition + i] = item;
      i++;
    }

    size++;
  }

  private void resize() {
    int newSize = getTupleSize() * (size + ALLOCATION_CHUNK_SIZE);
    long[] replacement = new long[newSize];

    System.arraycopy(tuples, 0, replacement, 0, tuples.length);
    Arrays.fill(replacement, tuples.length, replacement.length, EMPTY);
    tuples = replacement;
  }

  private long[] findSmallest(boolean remove) {
    if (size == 0) {
      return null;
    }

    int headIndex = getHeadIndex();
    long[] tupleAt = getTupleAt(headIndex);

    if (remove) {
      // mark this as empty
      tuples[headIndex] = EMPTY;
      // and set the head pointer
      setHeadIndex(headIndex + getTupleSize());
      size--;
    }

    return tupleAt;
  }

  private int getHeadIndex() {
    // the first element is overloaded to be a pointer to the head of the non-empty segment
    // of the tuples when it is negative
    if (tuples[0] < 0) {
      return (int) (-1 * tuples[0]);
    } else {
      return 0;
    }
  }

  private void setHeadIndex(int index) {
    if (tuples[0] < 0) {
      tuples[0] = -index;
    } else {
      throw new IllegalStateException(
          String.format("trying to set head index when not empty. value: %d", tuples[0]));
    }
  }

  /**
   * reads the current head index and updates if the new one is smaller
   *
   * @param insertLocation
   */
  private void updateHeadIndex(int insertLocation) {
    int headIndex = getHeadIndex();

    if (headIndex > insertLocation) {
      setHeadIndex(insertLocation);
    }
  }

  /**
   * ex: with getTupleSize()=2, and position 3, this returns 6
   *
   * @param position tuple position
   * @return position in flattened array
   */
  private int translate(int position) {
    return getTupleSize() * position;
  }

  private int invertTranslation(int translatedPosition) {
    return translatedPosition / getTupleSize();
  }

  private long[] getTupleAt(int translatedPosition) {
    long[] result = new long[getTupleSize()];

    System.arraycopy(tuples, translatedPosition, result, 0, getTupleSize());

    return result;
  }

  private class Iter implements Iterator<long[]> {
    private int position = getHeadIndex() / getTupleSize();
    private long[] nextValue = null;

    @Override
    public boolean hasNext() {
      boolean hasNext = true;

      synchronized (AbstractLongTupleList.this) {
        if (nextValue == null) {
          int translatedPosition = translate(position);
          int translatedSize = translate(size);

          while (translatedPosition < translatedSize && isEmpty(translatedPosition)) {
            translatedPosition += getTupleSize();
          }

          hasNext = translatedPosition < translatedSize;

          if (hasNext) {
            position = invertTranslation(translatedPosition);
            nextValue = getTupleAt(translatedPosition);
          }
        }
      }

      return hasNext;
    }

    @Override
    public long[] next() {
      synchronized (AbstractLongTupleList.this) {
        if (!hasNext()) {
          throw new NoSuchElementException(
              String.format("position: %d, nextValue %s", position, nextValue));
        }

        position++;

        long[] result = nextValue;
        // null out nextValue so hasNext() will fill it in
        nextValue = null;

        return result;
      }
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("remove not supported; read-only");
    }
  }
}
