package com.facebook.memory.data.types.definitions;

public class FixedSizeByteArraySlot extends FixedSizeSlot<FixedSizeByteArrayAccessor> {
  private final int length;

  public FixedSizeByteArraySlot(int length) {
    super(new FixedSizeSlotType(length));
    this.length = length;
  }

  @Override
  public FixedSizeByteArrayAccessor accessor(long baseAddress) {
    return new FixedSizeByteArrayAccessor(baseAddress, length, getSlotOffsetMapper());
  }

  @Override
  public FixedSizeByteArrayAccessor accessor(SlotAccessor previousSlotAccessor) {
    return new FixedSizeByteArrayAccessor(previousSlotAccessor, length, getSlotOffsetMapper());
  }
}
