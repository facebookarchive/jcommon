package com.facebook.memory.data.types.definitions;

import sun.misc.Unsafe;

import com.facebook.memory.UnsafeAccessor;

public class PointerAccessor extends AbstractSlotAccessor {
  private final Unsafe unsafe = UnsafeAccessor.get();

  public PointerAccessor(long baseAddress, FieldOffsetMapper fieldOffsetMapper) {
    super(baseAddress, fieldOffsetMapper);
  }

  public PointerAccessor(
    SlotAccessor previousSlotAccess,
    FieldOffsetMapper fieldOffsetMapper
  ) {
    super(previousSlotAccess, fieldOffsetMapper);
  }

  public void put(long pointer) {
    unsafe.putAddress(getSlotAddress(), pointer);
  }

  public long get() {
    return unsafe.getAddress(getSlotAddress());
  }
}
