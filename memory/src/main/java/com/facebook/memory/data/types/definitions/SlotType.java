package com.facebook.memory.data.types.definitions;

public interface SlotType {
  SlotSizeFunction getSlotSizeFunction();

  int getStaticSlotsSize();
}
