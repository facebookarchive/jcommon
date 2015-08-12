package com.facebook.memory.data.types.definitions;

import sun.misc.Unsafe;

import com.facebook.memory.UnsafeAccessor;

public class IntAccessor extends AbstractSlotAccessor {
  private static final Unsafe unsafe = UnsafeAccessor.get();

  public IntAccessor(long baseAddress, Slot slot) {
    super(baseAddress, slot);
  }

  public void put(int i) {
    unsafe.putInt(getOffset(), i);
  }

  public int get() {
    return unsafe.getInt(getOffset());
  }
}
