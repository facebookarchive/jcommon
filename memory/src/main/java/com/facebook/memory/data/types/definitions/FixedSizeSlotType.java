package com.facebook.memory.data.types.definitions;

public class FixedSizeSlotType implements SlotType {
  private final int size;

  public FixedSizeSlotType(int size) {
    this.size = size;
  }

  @Override
  public SlotSizeFunction getSlotSizeFunction() {
    return new FixedSlotSizeFunction(size);
  }

  @Override
  public int getStaticSlotsSize() {
    return size;
  }
}
