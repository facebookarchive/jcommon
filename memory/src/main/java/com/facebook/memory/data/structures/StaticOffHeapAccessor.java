package com.facebook.memory.data.structures;

/**
 * implementation
 * @param <T>
 */
public interface StaticOffHeapAccessor<T extends OffHeap> extends StaticOffHeapAllocator<T>, OffHeapWrapper<T> {
}
