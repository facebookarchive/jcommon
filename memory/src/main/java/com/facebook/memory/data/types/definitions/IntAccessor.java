package com.facebook.memory.data.types.definitions;

import sun.misc.Unsafe;

import com.facebook.memory.UnsafeAccessor;

public class IntAccessor extends AbstractSlotAccessor {
  private static final Unsafe unsafe = UnsafeAccessor.get();

  public IntAccessor(long baseAddress, SlotOffsetMapper slotOffsetMapper) {
    super(baseAddress, SlotType.INT.getStaticSlotsSize(), slotOffsetMapper);
  }

  public IntAccessor(
    SlotAccessor previousSlotAccess,
    SlotOffsetMapper slotOffsetMapper
  ) {
    super(previousSlotAccess, SlotType.INT.getStaticSlotsSize(), slotOffsetMapper);
  }

  public void put(int i) {
    unsafe.putInt(getSlotAddress(), i);
  }

  public int get() {
    return unsafe.getInt(getSlotAddress());
  }

}
