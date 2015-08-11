package com.facebook.memory.data.types.definitions;

import sun.misc.Unsafe;

import com.facebook.memory.UnsafeAccessor;

public class ByteArraySlotAccessor extends SlotAccessor {
  private static final Unsafe unsafe = UnsafeAccessor.get();

  protected ByteArraySlotAccessor(long baseAddress, Slot slot) {
    super(baseAddress, slot);
  }

  public void put(int position, byte value) {
    unsafe.putByte(getOffset() + position, value);
  }

  public byte get(int position) {
    return unsafe.getByte(getOffset() + position);
  }
}
