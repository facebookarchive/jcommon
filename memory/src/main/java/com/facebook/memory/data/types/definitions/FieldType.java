package com.facebook.memory.data.types.definitions;

import com.facebook.memory.MemoryConstants;
import com.facebook.memory.UnsafeAccessor;

public enum FieldType {
  INT(Integer.BYTES),
  LONG(Long.BYTES),
  ADDRESS(MemoryConstants.ADDRESS_SIZE),
  BYTE_ARRAY(address -> UnsafeAccessor.get().getInt(address) + Integer.BYTES)
  ,;

  private final FieldSizeFunction fieldSizeFunction;

  FieldType(FieldSizeFunction fieldSizeFunction) {
    this.fieldSizeFunction = fieldSizeFunction;
  }

  FieldType(int size) {
    this(new FixedFieldSizeFunction(size));
  }

  public FieldSizeFunction getFieldSizeFunction() {
    return fieldSizeFunction;
  }
}
