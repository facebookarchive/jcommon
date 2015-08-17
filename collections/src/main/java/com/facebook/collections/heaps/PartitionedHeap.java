package com.facebook.collections.heaps;

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
public class PartitionedHeap<T extends Comparable<? super T>> implements Heap<T> {
  private final HeapPartition<T>[] heapPartitions;
  private final Comparator<HeapPartition<T>> comparator;

  public PartitionedHeap(HeapPartition<T>[] heapPartitions, Comparator<T> comparator) {
    this.heapPartitions = heapPartitions;
    this.comparator = HeapPartition.comparatorFrom(comparator);
  }

  @Override
  public T last() {
    T last = null;

    for (int i = heapPartitions.length - 1; i >= 0 && last == null; i--) {
      last = operateOnHeap(i, p -> p.getSet().isEmpty() ? null : p.getSet().last());
    }

    return last;
  }

  @Override
  public T pollLast() {
    T last = null;

    for (int i = heapPartitions.length - 1; i >= 0 && last == null; i--) {
      last = operateOnHeap(i, p -> p.getSet().isEmpty() ? null : p.getSet().pollLast());
    }

    return last;
  }

  @Override
  public T higher(T item) {
    int index = findIndex(item);

    for (int i = index; i < heapPartitions.length; i++) {
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

    for (int i = index; i < heapPartitions.length; i++) {
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

    for (int i = 0; i < heapPartitions.length; i++) {
      size += operateOnHeap(i, p -> p.getSet().size());
    }

    return size;
  }

  @Override
  public boolean isEmpty() {
    for (int i = 0; i < heapPartitions.length; i++) {
      boolean partionIsEmpty = operateOnHeap(i, p -> p.getSet().isEmpty());
      if (!partionIsEmpty) {
        return false;
      }
    }

    return true;
  }

  private <R> R operateOnHeap(int index, HeapOperation<T, R> operation) {
    synchronized (heapPartitions[index]) {
      return operation.execute(heapPartitions[index]);
    }
  }

  private int findIndex(T value) {
    int shardIndex = Arrays.binarySearch(heapPartitions, HeapPartition.<T>keyOnlyShard(value), comparator);

    if (shardIndex < 0) {
      shardIndex = -(shardIndex + 1);
    }
    shardIndex = Math.max(Math.min(shardIndex, heapPartitions.length - 1), 0);

    return shardIndex;
  }

  private interface HeapOperation<K extends Comparable<? super K>, V> {
    V execute(HeapPartition<K> operation);
  }
}
