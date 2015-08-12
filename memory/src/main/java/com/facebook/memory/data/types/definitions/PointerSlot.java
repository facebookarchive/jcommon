package com.facebook.memory.data.types.definitions;

public class PointerSlot extends FixedSizeSlot<PointerAccessor> {
  public PointerSlot() {
    super(FieldType.ADDRESS);
  }

  @Override
  public PointerAccessor accessor(long baseAddress) {
    return new PointerAccessor(baseAddress, getFieldOffsetMapper());
  }

  @Override
  public PointerAccessor accessor(SlotAccessor previousSlotAccessor) {
    return new PointerAccessor(previousSlotAccessor, getFieldOffsetMapper());
  }
}
