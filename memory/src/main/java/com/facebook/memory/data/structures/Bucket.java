package com.facebook.memory.data.structures;

/**
 * Bucket in a hashmap. This is used to store elements in a map that collide
 */
import com.facebook.memory.FailedAllocationException;

public interface Bucket extends OffHeap {
  AnnotatedByteArray get (byte [] key);

  AnnotatableMemoryAddress put(byte[] k, byte[] v) throws FailedAllocationException;

  /**
   *
   * @param key
   * @return true iff the key existed
   */
  boolean remove(byte[] key) ;

  long size();

  long capacity();
}
