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

/**
 * The idea here is to provide a uniform interface for arrays so that we may implement data
 * structures on top of them seeming to use "Objects", with auto[un]boxing, but the backing storage
 * may be primitive arrays in order to save memory (ex: hash tables implemented in terms of arrays)
 *
 * <p>While this interface doesn't 100% enforce that implementations will be a class array, the
 * implementation should have array-like properties in terms of memory usage and time of operations:
 * get/setshould be O(1). resize should be O(n). append() should be O(1) amortized with set() calls
 * in that it's basically an iterator walking towards the end, and will insert values in the first
 * empty slot. A slot at it's ptr is not empty only if set was called, so you can see the # of total
 * set/append calls to fill a list is O(N), 2N in the pathological case (call set on all values and
 * leave the last empty; append will take O(N) time and then the array is full
 *
 * @param <T> type of the array. T = Long may in fact mean the backing storage could be long[] which
 *     saves 24 bytes per entry (the pointer + 2w overhead)
 *     <p>Each implementation defines if it's thread safe or not
 */
public interface Array<T> extends Iterable<T> {
  /**
   * gets element at position i. For the love of all that is good and holy, please make this O(1) in
   * all your implementations
   *
   * @param i position
   * @return value at position i, null if empty
   * @throws ArrayIndexOutOfBoundsException if array is too small, or i is negative
   */
  public T get(int i) throws IndexOutOfBoundsException;

  /**
   * return size of array (# of elements)
   *
   * @return
   */
  public int size();

  /**
   * return array capacity (size + empty slots) same as array.length in the case of built-in arrays
   *
   * @return
   */
  public int capacity();

  /**
   * replace a value. i must be < capacity()
   *
   * @param i location
   * @param value value to place, may NOT be null
   * @return previous value, null if nada
   * @throws IndexOutOfBoundsException i is >= size or i is negative
   */
  public T set(int i, T value) throws IndexOutOfBoundsException;

  /**
   * add an element to the end of the array. Almost always shorthand for:
   *
   * <pre>simpleArray.set(simpleArray.size(), value) </pre>
   *
   * (with an exception, see below)
   *
   * <p>this is useful when you want to use the array like queue or stack, and in practice some
   * one-off impls of classes like this did "append" only.
   *
   * <p>with the difference that if size() == capacity(), the array it is automatically grown to
   * accommodate the new value
   *
   * <p>In the case if a value has been set past the "end" of contiguous values, this will continue
   * and skip over set values and set the first empty value
   *
   * @param value to add, may NOT be null
   * @return position inserted at
   */
  public int append(T value);

  /**
   * remove a value at position i
   *
   * @param i position between 0 and size-
   * @return value at the position, null if it was empty
   * @throws ArrayIndexOutOfBoundsException i is >= size or i is negative
   */
  public T remove(int i) throws ArrayIndexOutOfBoundsException;

  /**
   * the idea here is that want to grow the array, and give a 'hint'. Note, underlying
   * implementations may not be able to create an array of the exact size For example, asking for 2
   * * size() might result in a slightly different size.
   *
   * <p>For shrinking: one should copy to a smaller array. Because shrinking would have unknown
   * effects on the indices of values known to the caller, there is not much value in having the
   * ability to shrink internalized
   *
   * @param sizeHint can be thought of as a new size to grow to, but implementations may not grow to
   *     exactly that size. The actual size is returned. Should be > 0
   * @return actual size of new array
   */
  public int resize(int sizeHint);
}
