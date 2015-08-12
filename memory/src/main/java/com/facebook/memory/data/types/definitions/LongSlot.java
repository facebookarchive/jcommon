package com.facebook.memory.data.types.definitions;

public class LongSlot extends Slot {
  public LongSlot() {
    super(FieldType.LONG);
  }

  @Override
  public LongAccessor accessor(long address) {
    return new LongAccessor(address, getFieldOffsetMapper());
  }

  @Override
  public LongAccessor accessor(SlotAccessor previousSlotAccessor) {
    return new LongAccessor(previousSlotAccessor, getFieldOffsetMapper());
  }
}
