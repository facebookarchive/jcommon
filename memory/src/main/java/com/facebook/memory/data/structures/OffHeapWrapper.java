package com.facebook.memory.data.structures;

public interface OffHeapWrapper<T extends OffHeap> {
  /**
   * given that an OffHeap object of type T was allocated at address, returns a java object that wraps the offheap
   * memory
   *
   * @param address
   * @return java object view of offheap data
   */
  T wrap(long address);
}
