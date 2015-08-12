package com.facebook.memory.data.types.definitions;

public abstract class AbstractSlotAccessor implements SlotAccessor {
  private final long baseAddress;
  private final Slot slot;

  protected AbstractSlotAccessor(long baseAddress, Slot slot) {
    this.baseAddress = baseAddress;
    this.slot = slot;
  }

  @Override
  public long getOffset() {
    return baseAddress + slot.getOffset();
  }
}
