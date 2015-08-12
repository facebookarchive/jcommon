package com.facebook.memory.data.types.definitions;

/**
 * manages the computation of slot offsets relative to a base address. For variable-sized slots, these are
 * chained so that proper calculations can be done
 */
public interface SlotOffsetMapper {
  /**
   * for a data structure (consisting of multiple slots) add address
   * @param address of a Struct (array of slots)
   * @return the offset from address for ths slot
   */
  int getSlotStartOffset(long address);

  /**
   *
   * @param address of the struct
   * @return the size of this slot
   */
  // TODO: get rid of this as calling this on a series of slots results in inefficient recursion
  int getSlotSize(long address);

  int getSlotSize(long address, int startOffset);
}
