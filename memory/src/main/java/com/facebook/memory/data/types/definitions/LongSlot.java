package com.facebook.memory.data.types.definitions;

public class LongSlot extends Slot {
  public LongSlot() {
    super(FieldType.LONG);
  }

  @Override
  public AbstractSlotAccessor accessor(long address) {
    return new LongAccessor(address, this);
  }
}
