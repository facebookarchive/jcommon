package com.facebook.memory.data.structures;

import com.facebook.memory.FailedAllocationException;

public interface OffHeapAccessor<T extends OffHeap> {
  /**
   *
   * @return a new instance of an OffHeap data structure
   *
   * @throws FailedAllocationException unable to allocate the object
   */
  T create() throws FailedAllocationException;

  /**
   * given that an OffHeap object of type T was allocated at address, returns the heap-version of said object
   * @param address
   * @return
   */
  T wrap(long address);
}
