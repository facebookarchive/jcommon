package com.facebook.memory.data.structures;

public interface DynamicOffHeapAccessor<T extends OffHeap> extends DynamicOffHeapAllocator<T>, OffHeapWrapper<T> {
}
