package com.facebook.collections.orderedset;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

/**
 * this allows some degree of concurrency for a Heap structure. It basically delegates to a pre-allocated set of
 * HeapPartition objects which are themselves NavigableSets used as heaps. Any add will
 *
 * @param <T>
 */

@ThreadSafe
public class PartitionedOrderedSet<T extends Comparable<? super T>> implements OrderedSet<T> {
  private final OrderedSetPartition<T>[] orderedSetPartitions;
  private final Comparator<OrderedSetPartition<T>> comparator;

  public PartitionedOrderedSet(OrderedSetPartition<T>[] orderedSetPartitions, Comparator<T> comparator) {
    this.orderedSetPartitions = orderedSetPartitions;
    this.comparator = OrderedSetPartition.comparatorFrom(comparator);
  }

  @Override
  public T last() {
    T last = null;

    for (int i = orderedSetPartitions.length - 1; i >= 0 && last == null; i--) {
      last = operateOnHeap(i, p -> p.getSet().isEmpty() ? null : p.getSet().last());
    }

    return last;
  }

  @Override
  public T pollLast() {
    T last = null;

    for (int i = orderedSetPartitions.length - 1; i >= 0 && last == null; i--) {
      last = operateOnHeap(i, p -> p.getSet().isEmpty() ? null : p.getSet().pollLast());
    }

    return last;
  }

  @Override
  public T higher(T item) {
    int index = findIndex(item);

    for (int i = index; i < orderedSetPartitions.length; i++) {
      T result = operateOnHeap(i, p -> p.getSet().isEmpty() ? null : p.getSet().higher(item));

      if (result != null) {
        return result;
      }
    }

    return null;
  }

  @Override
  public T ceiling(T item) {
    int index = findIndex(item);

    for (int i = index; i < orderedSetPartitions.length; i++) {
      T result = operateOnHeap(i, p -> p.getSet().isEmpty() ? null : p.getSet().ceiling(item));

      if (result != null) {
        return result;
      }
    }

    return null;
  }

  @Override
  public T lower(T item) {
    int index = Math.max(findIndex(item), 0);

    for (int i = index; i >= 0; i--) {
      T result = operateOnHeap(i, p -> p.getSet().isEmpty() ? null : p.getSet().lower(item));

      if (result != null) {
        return result;
      }
    }

    return null;
  }

  @Override
  public T floor(T item) {
    int index = Math.max(findIndex(item), 0);

    for (int i = index; i >= 0; i--) {
      T result = operateOnHeap(i, p -> p.getSet().isEmpty() ? null : p.getSet().floor(item));

      if (result != null) {
        return result;
      }
    }

    return null;
  }

  @Override
  public boolean remove(T item) {
    return operateOnHeap(findIndex(item), p -> p.getSet().remove(item));
  }

  @Override
  public boolean removeAll(Collection<T> items) {
    return items.stream()
      .map(item -> operateOnHeap(findIndex(item), p -> p.getSet().remove(item)))
      .reduce(true, (aBoolean, aBoolean2) -> aBoolean && aBoolean2);
  }

  @Override
  public boolean add(T item) {
    return operateOnHeap(findIndex(item), p -> p.getSet().add(item));
  }

  @Override
  public int size() {
    int size = 0;

    for (int i = 0; i < orderedSetPartitions.length; i++) {
      size += operateOnHeap(i, p -> p.getSet().size());
    }

    return size;
  }

  @Override
  public boolean isEmpty() {
    for (int i = 0; i < orderedSetPartitions.length; i++) {
      boolean partionIsEmpty = operateOnHeap(i, p -> p.getSet().isEmpty());
      if (!partionIsEmpty) {
        return false;
      }
    }

    return true;
  }

  private <R> R operateOnHeap(int index, HeapOperation<T, R> operation) {
    synchronized (orderedSetPartitions[index]) {
      return operation.execute(orderedSetPartitions[index]);
    }
  }

  private int findIndex(T value) {
    int shardIndex = Arrays.binarySearch(orderedSetPartitions, OrderedSetPartition.<T>keyOnlyShard(value), comparator);

    if (shardIndex < 0) {
      shardIndex = -(shardIndex + 1);
    }
    shardIndex = Math.max(Math.min(shardIndex, orderedSetPartitions.length - 1), 0);

    return shardIndex;
  }

  private interface HeapOperation<K extends Comparable<? super K>, V> {
    V execute(OrderedSetPartition<K> operation);
  }
}
