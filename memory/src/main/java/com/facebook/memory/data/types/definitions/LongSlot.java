package com.facebook.memory.data.types.definitions;

public class LongSlot extends Slot {
  public LongSlot() {
    super(FieldType.LONG);
  }

  @Override
  public SlotAccessor accessor(long address) {
    return null;
  }
}
