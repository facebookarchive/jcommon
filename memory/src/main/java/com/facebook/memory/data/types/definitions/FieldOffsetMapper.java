package com.facebook.memory.data.types.definitions;

public interface FieldOffsetMapper {
  int getFieldStartOffset(long address);

  int getFieldSize(long address);

  int getFieldSize(long address, int startOffset);
}
