package com.facebook.memory.data.types.definitions;

import sun.misc.Unsafe;

import com.facebook.memory.UnsafeAccessor;

public class OffsetIntSlotAccessor implements SlotAccessor {
  private static final Unsafe UNSAFE = UnsafeAccessor.get();

  private final long address;
  private final int offset;
  private final long baseAddress;

  public OffsetIntSlotAccessor(long baseAddress, int offset) {
    this.baseAddress = baseAddress;
    this.address = baseAddress + offset;
    this.offset = offset;
  }

  @Override
  public long getBaseAddress() {
    return baseAddress;
  }

  @Override
  public int getSlotSize() {
    return 0;
  }

  @Override
  public long getSlotAddress() {
    return address;
  }

  @Override
  public int getSlotOffset() {
    return offset;
  }

  public void put(int value) {
    UNSAFE.putInt(address, value);
  }

  public int get() {
    return UNSAFE.getInt(address);
  }
}
