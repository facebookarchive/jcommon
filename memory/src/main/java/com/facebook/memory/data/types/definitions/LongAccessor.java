package com.facebook.memory.data.types.definitions;

import sun.misc.Unsafe;

import com.facebook.memory.UnsafeAccessor;

public class LongAccessor extends AbstractSlotAccessor {
  private static final Unsafe unsafe = UnsafeAccessor.get();

  public LongAccessor(long baseAddress, SlotOffsetMapper slotOffsetMapper) {
    super(baseAddress, SlotType.LONG.getStaticSlotsSize(), slotOffsetMapper);
  }

  public LongAccessor(
    SlotAccessor previousSlotAccess,
    SlotOffsetMapper slotOffsetMapper
  ) {
    super(previousSlotAccess, SlotType.LONG.getStaticSlotsSize(), slotOffsetMapper);
  }

  public void put(long l) {
    unsafe.putLong(getSlotAddress(), l);
  }

  public long get() {
    return unsafe.getLong(getSlotAddress());
  }

}
