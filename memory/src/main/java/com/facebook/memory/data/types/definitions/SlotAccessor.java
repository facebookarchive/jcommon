package com.facebook.memory.data.types.definitions;

public interface SlotAccessor {
  long getBaseAddress();
  long getSlotAddress();
  int getSlotOffset();
  int getSlotSize();
}
