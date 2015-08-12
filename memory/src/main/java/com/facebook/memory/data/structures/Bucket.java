package com.facebook.memory.data.structures;

/**
 * Bucket in a hashmap. This is used to store elements in a map that collide
 */

import com.facebook.memory.FailedAllocationException;
import com.facebook.memory.data.BucketPutResult;
import com.facebook.memory.data.types.definitions.SizedOffHeapStructure;

public interface Bucket<K extends SizedOffHeapStructure, V extends SizedOffHeapStructure> extends OffHeap {
  BucketEntry get(K  key);

  BucketPutResult put(K key, V value) throws FailedAllocationException;

  /**
   * @param key
   * @return null if not present, else the removed entry
   */

  boolean remove(K key);

  long size();
}
