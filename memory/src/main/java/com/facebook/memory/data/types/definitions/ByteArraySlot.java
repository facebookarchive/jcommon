package com.facebook.memory.data.types.definitions;

public class ByteArraySlot extends DynamicallySizedSlot<ByteArraySlotAccessor> {
  public ByteArraySlot() {
    super(FieldType.BYTE_ARRAY);
  }

  @Override
  public ByteArraySlotAccessor create(long address, int length) {
    ByteArraySlotAccessor accessor = new ByteArraySlotAccessor(address, length, getFieldOffsetMapper());

    accessor.putLength(length);

    return accessor;
  }

  @Override
  public ByteArraySlotAccessor wrap(long baseAddress) {
    return ByteArraySlotAccessor.wrap(baseAddress, getFieldOffsetMapper());
  }

  @Override
  public ByteArraySlotAccessor create(SlotAccessor previousSlotAccessor, int length) {
    ByteArraySlotAccessor accessor = new ByteArraySlotAccessor(previousSlotAccessor, length, getFieldOffsetMapper());

    accessor.putLength(length);

    return accessor;
  }

  @Override
  public ByteArraySlotAccessor wrap(SlotAccessor previousSlotAccessor) {
    return ByteArraySlotAccessor.wrap(previousSlotAccessor, getFieldOffsetMapper());
  }
}
