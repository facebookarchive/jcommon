package com.facebook.memory.data.types.definitions;

import sun.misc.Unsafe;

import com.facebook.memory.UnsafeAccessor;

public class PointerAccessor extends SlotAccessor {
  private final Unsafe unsafe = UnsafeAccessor.get();

  public PointerAccessor(long baseAddress, Slot slot) {
    super(baseAddress, slot);
  }

  public void put(long pointer) {
    unsafe.putAddress(getOffset(), pointer);
  }

  public long get() {
    return unsafe.getAddress(getOffset());
  }
}
