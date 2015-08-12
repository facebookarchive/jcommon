package com.facebook.memory.data.types.definitions;

public class FixedSlotSizeFunction implements SlotSizeFunction {
  private final int size;

  public FixedSlotSizeFunction(int size) {
    this.size = size;
  }

  @Override
  public int getSize(long address) {
    return size;
  }
}
