package com.facebook.memory.data.types.definitions;

public class PointerSlot extends Slot {
  public PointerSlot() {
    super(FieldType.ADDRESS);
  }

  @Override
  public PointerAccessor accessor(long address) {
    return new PointerAccessor(address, this);
  }
}