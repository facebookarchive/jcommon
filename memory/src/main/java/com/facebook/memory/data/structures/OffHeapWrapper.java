package com.facebook.memory.data.structures;

public interface OffHeapWrapper<T extends OffHeap> {
  /**
   * given that an OffHeap object of type T was allocated at address, returns the heap-version of said object
   * @param address
   * @return
   */
  T wrap(long address);
}
