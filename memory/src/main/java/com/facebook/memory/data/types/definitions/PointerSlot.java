package com.facebook.memory.data.types.definitions;

public class PointerSlot extends FixedSizeSlot<PointerAccessor> {
  public PointerSlot() {
    super(SlotTypes.ADDRESS);
  }

  @Override
  public PointerAccessor accessor(long baseAddress) {
    return new PointerAccessor(baseAddress, getSlotOffsetMapper());
  }

  @Override
  public PointerAccessor accessor(SlotAccessor previousSlotAccessor) {
    return new PointerAccessor(previousSlotAccessor, getSlotOffsetMapper());
  }
}
