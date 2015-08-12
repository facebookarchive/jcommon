package com.facebook.memory.data.types.definitions;

public class IntSlot extends Slot<IntAccessor> {
  public IntSlot() {
    super(FieldType.INT);
  }

  @Override
  public IntAccessor accessor(long address) {
    return new IntAccessor(address, getFieldOffsetMapper());
  }

  @Override
  public IntAccessor accessor(SlotAccessor previousSlotAccess) {
    return new IntAccessor(previousSlotAccess, getFieldOffsetMapper());
  }
}
