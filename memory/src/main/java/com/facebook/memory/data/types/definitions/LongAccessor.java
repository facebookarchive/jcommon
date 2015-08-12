package com.facebook.memory.data.types.definitions;

import sun.misc.Unsafe;

import com.facebook.memory.UnsafeAccessor;

public class LongAccessor extends AbstractSlotAccessor {
  public final Unsafe unsafe = UnsafeAccessor.get();

  public LongAccessor(long baseAddress, FieldOffsetMapper fieldOffsetMapper) {
    super(baseAddress, fieldOffsetMapper);
  }

  public LongAccessor(
    SlotAccessor previousSlotAccess,
    FieldOffsetMapper fieldOffsetMapper
  ) {
    super(previousSlotAccess, fieldOffsetMapper);
  }

  public void put(long l) {
    unsafe.putLong(getSlotAddress(), l);
  }

  public Long get() {
    return unsafe.getLong(getSlotAddress());
  }
}
