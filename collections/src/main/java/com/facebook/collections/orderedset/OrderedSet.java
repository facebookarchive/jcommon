package com.facebook.collections.orderedset;

import java.util.Collection;

/**
 * subset of methods from NavigableSet
 *
 * @param <T>
 */
public interface OrderedSet<T extends Comparable<? super T>> {
  T last();

  T pollLast();

  T higher(T item);

  T ceiling(T item);

  T lower(T item);

  T floor(T item);

  boolean remove(T item);

  boolean removeAll(Collection<T> items);

  boolean add(T item);

  /**
   * @return count of items in the heap
   */
  int size();

  boolean isEmpty();
}
