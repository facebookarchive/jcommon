package com.facebook.memory.data.types.definitions;

import com.facebook.memory.MemoryConstants;

public enum FieldType {
  MEASURE(0),
  INT(Integer.BYTES),
  LONG(Long.BYTES),
  ADDRESS(MemoryConstants.ADDRESS_SIZE),
  BYTE_ARRAY(0, true),
  ;

  private final int size;
  private final boolean isTerminal;

  FieldType(int size, boolean isTerminal) {
    this.size = size;
    this.isTerminal = isTerminal;
  }

  FieldType(int size) {
    this(size, false);
  }

  public int getSize() {
    return size;
  }

  public boolean isTerminal() {
    return isTerminal;
  }
}
