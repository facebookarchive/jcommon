package com.facebook.memory.data.types.definitions;

public abstract class AbstractSlotAccessor implements SlotAccessor {
  private final int slotOffset;
  private final long slotAddress;
  private final int size;
  private final long baseAddress;

  /**
   * @param baseAddress the base address of the data structure to which this slot belongs
   * @param size total size of this slot
   * @param slotOffsetMapper
   */
  protected AbstractSlotAccessor(long baseAddress, int size, SlotOffsetMapper slotOffsetMapper) {
    this.baseAddress = baseAddress;
    this.size = size;
    slotOffset = slotOffsetMapper.getSlotStartOffset(baseAddress);
    slotAddress = baseAddress + slotOffset;
  }

  /**
   * this uses a previously bound SlotAccessor to use the end of the previous slot to unroll recursive calls
   * to getFieldStartOffset and getFieldSize.
   * <p>
   * SlotAccessor MUST be bound to the same address, or chaos ensues
   *
   * @param previousSlotAccessor
   * @param size total size of this data slot
   * @param slotOffsetMapper
   */
  protected AbstractSlotAccessor(
    SlotAccessor previousSlotAccessor,
    int size,
    SlotOffsetMapper slotOffsetMapper
  ) {
    this(
      previousSlotAccessor.getBaseAddress(),
      size,
      new PartiallyBoundOffsetMapper(slotOffsetMapper, previousSlotAccessor)
    );
  }

  @Override
  public long getBaseAddress() {
    return baseAddress;
  }

  @Override
  public long getSlotAddress() {
    return slotAddress;
  }

  @Override
  public int getSlotOffset() {
    return slotOffset;
  }

  @Override
  public int getSlotSize() {
    return size;
  }

  @Override
  public String toString() {
    return "AbstractSlotAccessor{" +
      "slotOffset=" + slotOffset +
      ", slotAddress=" + slotAddress +
      ", size=" + size +
      ", baseAddress=" + baseAddress +
      '}';
  }
}
