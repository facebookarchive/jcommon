package com.facebook.memory.data.structures;

import com.facebook.memory.FailedAllocationException;


public interface OffHeapAllocator<T extends OffHeap> {
  /**
   * @return a new instance of an OffHeap data structure
   *
   * @throws FailedAllocationException unable to allocate the object
   */
  T create() throws FailedAllocationException;
}
