package com.facebook.memory.data.types.definitions;

public abstract class DynamicallySizedSlot<T extends AbstractSlotAccessor> extends Slot {
  protected DynamicallySizedSlot(SlotType slotType) {
    super(slotType);
  }
  public abstract T create(long baseAddress, int size);

  /**
   * this assumes the data is already populated
   * @param baseAddress
   * @return
   */
  public abstract T wrap (long baseAddress);

  /**
   * this is a faster method to create an accessor based on the previous slot/field in the struct
   *
   * @param previousSlotAccess previously bound to the base adddress
   * @return
   */
  public abstract T create(SlotAccessor previousSlotAccessor, int size);

  public abstract T wrap(SlotAccessor previousSlotAccessor);
}
