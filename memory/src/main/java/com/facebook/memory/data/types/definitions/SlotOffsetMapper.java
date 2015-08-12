package com.facebook.memory.data.types.definitions;

/**
 * manages the computation of slot/field offsets relative to a base address. For variable-sized slots, these are
 * chained so that proper calculations can be done
 */
public interface SlotOffsetMapper {
  /**
   *
   * @param address
   * @return
   */
  int getSlotStartOffset(long address);

  // TODO: get rid of this as calling this on a series of slots results in inefficient recursion
  int getSlotSize(long address);

  int getSlotSize(long address, int startOffset);
}
