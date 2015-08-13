package com.facebook.memory.data.types.definitions;

import sun.misc.Unsafe;

import com.facebook.memory.UnsafeAccessor;
import com.facebook.memory.data.structures.OffHeapByteArray;
import com.facebook.memory.data.structures.OffHeapByteArrayImpl;

public class ByteArraySlotAccessor extends AbstractSlotAccessor {
  private static final Unsafe unsafe = UnsafeAccessor.get();

  protected ByteArraySlotAccessor(long baseAddress, int length, FieldOffsetMapper fieldOffsetMapper) {
    super(baseAddress, length + Integer.BYTES, fieldOffsetMapper);
  }

  public ByteArraySlotAccessor(
    SlotAccessor previousSlotAccess,
    int length,
    FieldOffsetMapper fieldOffsetMapper
  ) {
    super(previousSlotAccess, length + Integer.BYTES, fieldOffsetMapper);
  }

  public static ByteArraySlotAccessor wrap(long baseAddress, FieldOffsetMapper fieldOffsetMapper) {
    int fieldStartOffset = fieldOffsetMapper.getFieldStartOffset(baseAddress);
    long thisAddress = baseAddress + fieldStartOffset;
    int length = UnsafeAccessor.get().getInt(thisAddress);

    return new ByteArraySlotAccessor(baseAddress, length, fieldOffsetMapper);
  }

  public static ByteArraySlotAccessor wrap(SlotAccessor previousSlotAccessor, FieldOffsetMapper fieldOffsetMapper) {
    long previousSlotAddress = previousSlotAccessor.getSlotAddress();
    long previousSlotSize = previousSlotAccessor.getSlotSize();
    long thisAddress = previousSlotAddress + previousSlotSize;
    int length = UnsafeAccessor.get().getInt(thisAddress);

    return new ByteArraySlotAccessor(previousSlotAccessor, length, fieldOffsetMapper);
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
