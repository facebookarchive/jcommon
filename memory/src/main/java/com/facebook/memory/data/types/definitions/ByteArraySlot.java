package com.facebook.memory.data.types.definitions;

public class ByteArraySlot extends Slot<ByteArraySlotAccessor> {
  public ByteArraySlot() {
    super(FieldType.BYTE_ARRAY);
  }

  @Override
  public ByteArraySlotAccessor accessor(long address) {
    return new ByteArraySlotAccessor(address, this);
  }
}
