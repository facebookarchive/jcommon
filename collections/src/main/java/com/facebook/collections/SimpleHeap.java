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

import java.util.Collection;

/**
 * 
 * @param <T> type being held. Implementations will require that T extend Comparable or
 * provide a Comparator
 * 
 */
public interface SimpleHeap<T> extends Iterable<T>{
  /**
   * does not alter the heap, read-only method
   * 
   * 
   * @return the top of the heap, or least according to comparison order; null if empty  heap
   */
  public T peek();

  /**
   * removes the top of the heap
   * 
   * @return the top of the heap, or least according to comparison order; null if empty heap
   */
  public T poll();

  /**
   * adds an item to the heap.  
   * @param item
   * @return true iff only added (ex: heap impls may be bounded)
   */
  public boolean add(T item);

  /**
   * adds a collection of items to the head of the heap
   * 
   * @param items 
   * @return true iff at least one item was added
   */
  public boolean addAll(Collection<? extends T> items);

  /**
   * 
   * @return  number of elements in the heap
   */
  public int size();

  /**
   * optional method, may be a no-op
   * 
   * if implemented, shrinks the heap's memory footprint as much as possible
   * 
   * @return may return the # of slots or bytes saved. A return value of 0 need not indicate
   * 0 saved, per see. See implementation's documentation
   */
  public int shrink();

  /**
   * makes a copy of the heap. May be faster than iterating and calling add() or addAll()
   * 
   * @return copy of the heap. 
   */
  public SimpleHeap<T> makeCopy();
}
