package com.facebook.memory.data.types.definitions;

import sun.misc.Unsafe;

import com.facebook.memory.UnsafeAccessor;

public class IntAccessor extends AbstractSlotAccessor {
  private static final Unsafe unsafe = UnsafeAccessor.get();

  public IntAccessor(long baseAddress, FieldOffsetMapper fieldOffsetMapper) {
    super(baseAddress, FieldType.INT.getStaticFieldsSize(), fieldOffsetMapper);
  }

  public IntAccessor(
    SlotAccessor previousSlotAccess,
    FieldOffsetMapper fieldOffsetMapper
  ) {
    super(previousSlotAccess, FieldType.INT.getStaticFieldsSize(), fieldOffsetMapper);
  }

  public void put(int i) {
    unsafe.putInt(getSlotAddress(), i);
  }

  public int get() {
    return unsafe.getInt(getSlotAddress());
  }

}
