package com.facebook.memory.data.types.definitions;

public class ByteArraySlot extends DynamicallySizedSlot<ByteArrayAccessor> {
  public ByteArraySlot() {
    super(SlotTypes.BYTE_ARRAY);
  }

  @Override
  public ByteArrayAccessor create(long address, int length) {
    ByteArrayAccessor accessor = new ByteArrayAccessor(address, length, getSlotOffsetMapper());

    accessor.putLength(length);

    return accessor;
  }

  @Override
  public ByteArrayAccessor wrap(long baseAddress) {
    return ByteArrayAccessor.wrap(baseAddress, getSlotOffsetMapper());
  }

  @Override
  public ByteArrayAccessor create(SlotAccessor previousSlotAccessor, int length) {
    ByteArrayAccessor accessor = new ByteArrayAccessor(previousSlotAccessor, length, getSlotOffsetMapper());

    accessor.putLength(length);

    return accessor;
  }

  @Override
  public ByteArrayAccessor wrap(SlotAccessor previousSlotAccessor) {
    return ByteArrayAccessor.wrap(previousSlotAccessor, getSlotOffsetMapper());
  }
}
