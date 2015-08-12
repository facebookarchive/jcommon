package com.facebook.memory.data.types.definitions;

import com.facebook.memory.MemoryConstants;
import com.facebook.memory.UnsafeAccessor;

public enum SlotType {
  INT(Integer.BYTES),
  LONG(Long.BYTES),
  ADDRESS(MemoryConstants.ADDRESS_SIZE),
  BYTE_ARRAY(Integer.BYTES, address ->
    UnsafeAccessor.get().getInt(address) + Integer.BYTES // size of data + integer for that size
  ),
  ;

  private final int staticFieldsSize;
  private final SlotSizeFunction slotSizeFunction;

  SlotType(int staticFieldsSize, SlotSizeFunction slotSizeFunction) {
    this.staticFieldsSize = staticFieldsSize;
    this.slotSizeFunction = slotSizeFunction;
  }

  SlotType(int size) {
    this(size, new FixedSlotSizeFunction(size));
  }

  public SlotSizeFunction getSlotSizeFunction() {
    return slotSizeFunction;
  }

  public int getStaticSlotsSize() {
    return staticFieldsSize;
  }
}
