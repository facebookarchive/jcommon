package com.facebook.memory.data.types.definitions;

public class LongSlot extends FixedSizeSlot<LongAccessor> {
  public LongSlot() {
    super(SlotType.LONG);
  }

  @Override
  public LongAccessor accessor(long baseAddress) {
    return new LongAccessor (baseAddress, getSlotOffsetMapper());
  }

  @Override
  public LongAccessor accessor(SlotAccessor previousSlotAccessor) {
    return new LongAccessor(previousSlotAccessor, getSlotOffsetMapper());
  }
}
