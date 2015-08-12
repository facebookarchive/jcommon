package com.facebook.memory.data.types.definitions;

import com.facebook.memory.MemoryConstants;
import com.facebook.memory.UnsafeAccessor;

public enum SlotTypes implements SlotType {
  INT(Integer.BYTES),
  LONG(Long.BYTES),
  ADDRESS(MemoryConstants.ADDRESS_SIZE),
  BYTE_ARRAY(Integer.BYTES, address ->
    UnsafeAccessor.get().getInt(address) + Integer.BYTES // size of data + integer for that size
  ),
  ;

  private final int staticSlotsSize;
  private final SlotSizeFunction slotSizeFunction;

  SlotTypes(int staticSlotsSize, SlotSizeFunction slotSizeFunction) {
    this.staticSlotsSize = staticSlotsSize;
    this.slotSizeFunction = slotSizeFunction;
  }

  SlotTypes(int size) {
    this(size, new FixedSlotSizeFunction(size));
  }

  @Override
  public SlotSizeFunction getSlotSizeFunction() {
    return slotSizeFunction;
  }

  @Override
  public int getStaticSlotsSize() {
    return staticSlotsSize;
  }
}
