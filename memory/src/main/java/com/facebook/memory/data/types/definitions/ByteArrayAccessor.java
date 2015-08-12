package com.facebook.memory.data.types.definitions;

import sun.misc.Unsafe;

import com.facebook.memory.UnsafeAccessor;
import com.facebook.memory.data.structures.OffHeapByteArray;
import com.facebook.memory.data.structures.OffHeapByteArrayImpl;

public class ByteArrayAccessor extends AbstractSlotAccessor {
  private static final Unsafe unsafe = UnsafeAccessor.get();

  protected ByteArrayAccessor(long baseAddress, int length, SlotOffsetMapper slotOffsetMapper) {
    super(baseAddress, length + Integer.BYTES, slotOffsetMapper);
  }

  public ByteArrayAccessor(
    SlotAccessor previousSlotAccess,
    int length,
    SlotOffsetMapper slotOffsetMapper
  ) {
    super(previousSlotAccess, length + Integer.BYTES, slotOffsetMapper);
  }

  public static ByteArrayAccessor wrap(long baseAddress, SlotOffsetMapper slotOffsetMapper) {
    int fieldStartOffset = slotOffsetMapper.getSlotStartOffset(baseAddress);
    long thisAddress = baseAddress + fieldStartOffset;
    int length = UnsafeAccessor.get().getInt(thisAddress);

    return new ByteArrayAccessor(baseAddress, length, slotOffsetMapper);
  }

  public static ByteArrayAccessor wrap(SlotAccessor previousSlotAccessor, SlotOffsetMapper slotOffsetMapper) {
    long previousSlotAddress = previousSlotAccessor.getSlotAddress();
    long previousSlotSize = previousSlotAccessor.getSlotSize();
    long thisAddress = previousSlotAddress + previousSlotSize;
    int length = UnsafeAccessor.get().getInt(thisAddress);

    return new ByteArrayAccessor(previousSlotAccessor, length, slotOffsetMapper);
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

  public OffHeapByteArray asByteArray() {
    return OffHeapByteArrayImpl.wrap(getSlotAddress());
  }
}
