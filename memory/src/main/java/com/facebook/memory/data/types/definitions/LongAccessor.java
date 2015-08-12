package com.facebook.memory.data.types.definitions;

import sun.misc.Unsafe;

import com.facebook.memory.UnsafeAccessor;

public class LongAccessor extends AbstractSlotAccessor {
  public final Unsafe unsafe = UnsafeAccessor.get();

  public LongAccessor(long baseAddress, Slot slot) {
    super(baseAddress, slot);
  }

  public void put(long l) {
    unsafe.putLong(getOffset(), l);
  }

  public Long get() {
    return unsafe.getLong(getOffset());
  }
}
