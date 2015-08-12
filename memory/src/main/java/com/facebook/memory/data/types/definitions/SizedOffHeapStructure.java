package com.facebook.memory.data.types.definitions;

public interface SizedOffHeapStructure extends OffHeapStructure {
  /**
   * @return size of the OffHeap data structure
   */
  int getSize();
}
