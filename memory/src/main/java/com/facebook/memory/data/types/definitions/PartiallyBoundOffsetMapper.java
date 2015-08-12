package com.facebook.memory.data.types.definitions;

class PartiallyBoundOffsetMapper implements FieldOffsetMapper {
  private final FieldOffsetMapper fieldOffsetMapper;
  private final int thisFieldStartOffset;

  PartiallyBoundOffsetMapper(FieldOffsetMapper fieldOffsetMapper, SlotAccessor previousSlotAccess) {
    this.fieldOffsetMapper = fieldOffsetMapper;
    thisFieldStartOffset = previousSlotAccess.getSlotOffset() + previousSlotAccess.getSlotSize();
  }

  @Override
  public int getFieldStartOffset(long address) {
    return thisFieldStartOffset;
  }

  @Override
  public int getFieldSize(long address) {
    return fieldOffsetMapper.getFieldSize(address, thisFieldStartOffset);
  }

  @Override
  public int getFieldSize(long address, int startOffset) {
    return fieldOffsetMapper.getFieldSize(address, startOffset);
  }
}
