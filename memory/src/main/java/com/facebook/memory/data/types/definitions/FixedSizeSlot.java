package com.facebook.memory.data.types.definitions;

public abstract class FixedSizeSlot<T extends AbstractSlotAccessor> extends Slot {
  protected FixedSizeSlot(FieldType fieldType) {
    super(fieldType);
  }

  public abstract T accessor(long baseAddress);

  /**
   * this is a faster method to create an accessor based on the previous slot/field in the struct
   *
   * @param previousSlotAccess previously bound to the base adddress
   * @return
   */
  public abstract T accessor(SlotAccessor previousSlotAccessor);
}
