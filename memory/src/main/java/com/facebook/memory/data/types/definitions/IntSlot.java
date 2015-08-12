package com.facebook.memory.data.types.definitions;

public class IntSlot extends FixedSizeSlot<IntAccessor> {
  public IntSlot() {
    super(FieldType.INT);
  }

  @Override
  public IntAccessor accessor(long baseAddress) {
    return new IntAccessor(baseAddress, getFieldOffsetMapper());
  }

  @Override
  public IntAccessor accessor(SlotAccessor previousSlotAccessor) {
    return new IntAccessor(previousSlotAccessor, getFieldOffsetMapper());
  }
}
