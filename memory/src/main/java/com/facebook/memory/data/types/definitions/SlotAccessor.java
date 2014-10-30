package com.facebook.memory.data.types.definitions;

public abstract class SlotAccessor {
  private final long baseAddress;
  private final Slot slot;

  protected SlotAccessor(long baseAddress, Slot slot) {
    this.baseAddress = baseAddress;
    this.slot = slot;
  }

  protected long getOffset() {
    return baseAddress + slot.getOffset();
  }
}
