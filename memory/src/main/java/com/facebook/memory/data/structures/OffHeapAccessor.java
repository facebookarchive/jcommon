package com.facebook.memory.data.structures;

public interface OffHeapAccessor<T extends OffHeap> {
  T create();
  T wrap(long address);
}
