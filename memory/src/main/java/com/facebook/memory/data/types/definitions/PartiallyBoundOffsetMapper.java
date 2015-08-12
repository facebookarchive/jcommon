package com.facebook.memory.data.types.definitions;

/**
 * we compute our start offset at construction, and hence are "partially bound". We do not compute our size until
 * asked to do so via one of the getSize() functions (most likely called by the Slot after us, if there is one).
 */
class PartiallyBoundOffsetMapper implements SlotOffsetMapper {
  private final SlotOffsetMapper slotOffsetMapper;
  private final int thisFieldStartOffset;

  PartiallyBoundOffsetMapper(SlotOffsetMapper slotOffsetMapper, SlotAccessor previousSlotAccessor) {
    this.slotOffsetMapper = slotOffsetMapper;
    thisFieldStartOffset = previousSlotAccessor.getSlotOffset() + previousSlotAccessor.getSlotSize();
  }

  @Override
  public int getSlotStartOffset(long address) {
    return thisFieldStartOffset;
  }

  @Override
  public int getSlotSize(long address) {
    return slotOffsetMapper.getSlotSize(address, thisFieldStartOffset);
  }

  @Override
  public int getSlotSize(long address, int startOffset) {
    return slotOffsetMapper.getSlotSize(address, startOffset);
  }
}
