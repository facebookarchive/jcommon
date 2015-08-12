package com.facebook.memory.data.structures;

/**
 * this is a marker interface. An Accessor provies the ability to both allocate new (emtpy) objects, as well as
 * wrap existing ones
 */
public interface OffHeapAccessor<T extends OffHeap> extends OffHeapAllocator<T>, OffHeapWrapper<T> {
}
