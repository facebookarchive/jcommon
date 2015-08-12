package com.facebook.memory.data.types.definitions;

import com.facebook.memory.MemoryConstants;

public enum FieldType {
  MEASURE(0, false, false),
  INT(Integer.BYTES),
  LONG(Long.BYTES),
  ADDRESS(MemoryConstants.ADDRESS_SIZE),
  DYNAMIC(0, false, false),
  BYTE_ARRAY(0, true, true),
  ;

  private final int size;
  private final boolean isTerminal;
  private final boolean updatesStruct;

  FieldType(int size, boolean isTerminal, boolean updatesStruct) {
    this.size = size;
    this.isTerminal = isTerminal;
    this.updatesStruct = updatesStruct;
  }

  FieldType(int size) {
    this(size, false, true);
  }

  public int getSize() {
    return size;
  }

  public boolean isTerminal() {
    return isTerminal;
  }

  public boolean isUpdatesStruct() {
    return updatesStruct;
  }
}
