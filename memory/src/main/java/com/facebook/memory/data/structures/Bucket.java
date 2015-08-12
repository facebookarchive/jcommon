package com.facebook.memory.data.structures;

/**
 * Bucket in a hashmap. This is used to store elements in a map that collide
 */

import com.facebook.collections.bytearray.ByteArray;
import com.facebook.memory.FailedAllocationException;

public interface Bucket extends OffHeap {
  AnnotatedByteArray get(ByteArray key);

  AnnotatableMemoryAddress put(ByteArray key, ByteArray value) throws FailedAllocationException;

  /**
   * @param key
   * @return true iff the key existed
   */

  boolean remove(ByteArray key);

  long size();
}
