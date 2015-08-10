package com.facebook.memory.data.types.definitions;

public class IntSlot extends Slot<IntAccessor> {
  public IntSlot() {
    super(FieldType.INT);
  }

  public IntAccessor accessor(long address) {
    return new IntAccessor(address, this);
  }
}
