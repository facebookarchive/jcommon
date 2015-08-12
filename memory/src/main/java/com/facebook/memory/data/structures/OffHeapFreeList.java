package com.facebook.memory.data.structures;

import sun.misc.Unsafe;

import java.util.Set;

import com.facebook.memory.UnsafeAccessor;

/**
 * TODO: implement memory-efficient storage of free-lists as a red-black tree in offheap storage
 *
 */
public class OffHeapFreeList implements FreeList {
  private static final Unsafe UNSAFE = UnsafeAccessor.get();

  @Override
  public void extend(int size) {

  }

  @Override
  public void free(int offset, int size) {

  }

  @Override
  public int allocate(int size) {
    return 0;
  }

  @Override
  public Set<IntRange> asRangeSet() {
    return null;
  }

  @Override
  public void reset(int size) {

  }
}
