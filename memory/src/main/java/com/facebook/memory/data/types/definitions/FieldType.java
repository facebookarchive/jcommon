package com.facebook.memory.data.types.definitions;

import com.facebook.memory.MemoryConstants;

public enum FieldType {
  MEASURE(0),
  INT(Integer.BYTES),
  LONG(Long.BYTES),
  ADDRESS(MemoryConstants.ADDRESS_SIZE),
  ;

  private final int size;

  FieldType(int size) {
    this.size = size;
  }

  public int getSize() {
    return size;
  }
}
