package com.facebook.memory.data.types.definitions;

import sun.misc.Unsafe;

import com.facebook.memory.UnsafeAccessor;
import com.facebook.memory.data.structures.FixedSizeOffHeapByteArray;
import com.facebook.memory.data.structures.OffHeapByteArray;

public class FixedSizeByteArrayAccessor extends AbstractSlotAccessor {
  private static final Unsafe unsafe = UnsafeAccessor.get();

  protected FixedSizeByteArrayAccessor(long baseAddress, int length, SlotOffsetMapper slotOffsetMapper) {
    super(baseAddress, length, slotOffsetMapper);
  }

  public FixedSizeByteArrayAccessor(
    SlotAccessor previousSlotAccess,
    int length,
    SlotOffsetMapper slotOffsetMapper
  ) {
    super(previousSlotAccess, length, slotOffsetMapper);
  }

  public void put(int position, byte value) {
    unsafe.putByte(getSlotAddress() + position, value);
  }

  public byte get(int position) {
    return unsafe.getByte(getSlotAddress() + position);
  }

  public int getLength() {
    return getSlotSize();
  }

  public OffHeapByteArray asByteArray() {
    return FixedSizeOffHeapByteArray.wrap(getSlotAddress(), getSlotSize());
  }
}
