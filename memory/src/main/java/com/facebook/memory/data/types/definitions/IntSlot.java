package com.facebook.memory.data.types.definitions;

public class IntSlot extends FixedSizeSlot<IntAccessor> {
  public IntSlot() {
    super(SlotType.INT);
  }

  @Override
  public IntAccessor accessor(long baseAddress) {
    return new IntAccessor(baseAddress, getSlotOffsetMapper());
  }

  @Override
  public IntAccessor accessor(SlotAccessor previousSlotAccessor) {
    return new IntAccessor(previousSlotAccessor, getSlotOffsetMapper());
  }
}
