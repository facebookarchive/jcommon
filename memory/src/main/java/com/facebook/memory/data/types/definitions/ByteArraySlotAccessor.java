package com.facebook.memory.data.types.definitions;

import sun.misc.Unsafe;

import com.facebook.memory.UnsafeAccessor;

public class ByteArraySlotAccessor extends AbstractSlotAccessor {
  private static final Unsafe unsafe = UnsafeAccessor.get();

  protected ByteArraySlotAccessor(long baseAddress, FieldOffsetMapper fieldOffsetMapper) {
    super(baseAddress, fieldOffsetMapper);
  }

  public ByteArraySlotAccessor(
    SlotAccessor previousSlotAccess,
    FieldOffsetMapper fieldOffsetMapper
  ) {
    super(previousSlotAccess, fieldOffsetMapper);
  }

  public void put(int position, byte value) {
    unsafe.putByte(getSlotAddress() + Integer.BYTES + position, value);
  }

  public byte get(int position) {
    return unsafe.getByte(getSlotAddress() + Integer.BYTES + position);
  }

  public void putLength(int length) {
    unsafe.putInt(getSlotAddress(), length);
  }

  public int getLength() {
    return unsafe.getInt(getSlotAddress());
  }
}
