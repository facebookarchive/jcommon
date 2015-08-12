package com.facebook.memory.data.types.definitions;

public class RelativeSlotOffsetMapper implements SlotOffsetMapper {
  private final SlotOffsetMapper previousSlotOffsetMapper;
  private final SlotSizeFunction slotSizeFunction;

  /**
   *
   * @param previousSlotOffsetMapper - SlotOffsetMapper bound before us in the struct. We can query it to find out
   *                                 where it ends and figure out where our slot begins
   * @param slotSizeFunction - function to determine
   */
  public RelativeSlotOffsetMapper(
    SlotOffsetMapper previousSlotOffsetMapper,
    SlotSizeFunction slotSizeFunction
  ) {
    this.previousSlotOffsetMapper = previousSlotOffsetMapper;
    this.slotSizeFunction = slotSizeFunction;
  }

  public RelativeSlotOffsetMapper(SlotSizeFunction slotSizeFunction) {
    this(new NullSlotOffsetMapper(), slotSizeFunction);
  }

  @Override
  public int getSlotStartOffset(long address) {
    int fieldStart = previousSlotOffsetMapper.getSlotStartOffset(address);
    int fieldSize = previousSlotOffsetMapper.getSlotSize(address);

    return fieldStart + fieldSize;
  }

  @Override
  public int getSlotSize(long address) {
    return getSlotSize(address, getSlotStartOffset(address));
  }

  @Override
  public int getSlotSize(long address, int fieldStartOffset) {
    return slotSizeFunction.getSize(address + fieldStartOffset);
  }

  private static class NullSlotOffsetMapper implements SlotOffsetMapper {
    @Override
    public int getSlotStartOffset(long address) {
      return 0;
    }

    @Override
    public int getSlotSize(long address) {
      return 0;
    }

    @Override
    public int getSlotSize(long address, int startOffset) {
      return 0;
    }
  }
}
