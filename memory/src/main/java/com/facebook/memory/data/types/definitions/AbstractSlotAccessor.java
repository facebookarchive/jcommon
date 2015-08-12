package com.facebook.memory.data.types.definitions;

public abstract class AbstractSlotAccessor implements SlotAccessor {
  private final int slotOffset;
  private final long slotAddress;
  private final int size;
  private final long baseAddress;

  protected AbstractSlotAccessor(long baseAddress, FieldOffsetMapper fieldOffsetMapper) {
    this.baseAddress = baseAddress;
    slotOffset = fieldOffsetMapper.getFieldStartOffset(baseAddress);
    slotAddress = baseAddress + slotOffset;
    size = fieldOffsetMapper.getFieldSize(baseAddress);
  }

  /**
   * this uses a previously bound SlotAccessor to use the end of the previous slot to unroll recursive calls
   * to getFieldStartOffset and getFieldSize.
   * <p>
   * SlotAccessor MUST be bound to the same address, or chaos ensues
   *
   * @param baseAddress
   * @param fieldOffsetMapper
   * @param previousSlotAccess
   */
  protected AbstractSlotAccessor(
    SlotAccessor previousSlotAccess,
    FieldOffsetMapper fieldOffsetMapper
  ) {
    this(previousSlotAccess.getBaseAddress(), new PartiallyBoundOffsetMapper(fieldOffsetMapper, previousSlotAccess));
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
      '}';
  }
}
