package com.facebook.memory.data.structures;

import com.facebook.memory.FailedAllocationException;

public interface DynamicOffHeapAllocator<T extends OffHeap> extends OffHeapWrapper<T> {
  /**
   * @param size hint about how much initial space to allocate
   * @return a new instance of an OffHeap data structure
   * @throws FailedAllocationException unable to allocate the object
   */
  T create(int size) throws FailedAllocationException;
}
