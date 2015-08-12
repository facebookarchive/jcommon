package com.facebook.memory.data.types.definitions;

import sun.misc.Unsafe;

import com.facebook.memory.UnsafeAccessor;

public class OffsetIntSlotAccessor implements SlotAccessor {
  private static final Unsafe UNSAFE = UnsafeAccessor.get();

  private final long address;
  private final int offset;

  public OffsetIntSlotAccessor(long address, int offset) {
    this.address = address;
    this.offset = offset;
  }

  @Override
  public long getOffset() {
    return offset;
  }

  public void put(int value) {
    UNSAFE.putInt(address + offset, value);
  }

  public int get() {
    return UNSAFE.getInt(address + offset);
  }
}
