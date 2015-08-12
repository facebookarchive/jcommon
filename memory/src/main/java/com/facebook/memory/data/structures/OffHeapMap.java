package com.facebook.memory.data.structures;

import com.facebook.memory.FailedAllocationException;

public interface OffHeapMap<K, V> {
  V get(K key);

  void put(K key, V value) throws FailedAllocationException;

  boolean remove(K key);

  boolean containsKey(K key);

  int getSize();
}
