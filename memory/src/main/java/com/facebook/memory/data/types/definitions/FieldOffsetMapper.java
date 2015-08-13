package com.facebook.memory.data.types.definitions;

public interface FieldOffsetMapper {
  int getFieldStartOffset(long address);

  // TODO: get rid of this as calling this on a series of slots results in inefficient recursion
  int getFieldSize(long address);

  int getFieldSize(long address, int startOffset);
}
