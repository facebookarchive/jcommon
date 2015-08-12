package com.facebook.memory.data.types.definitions;

import com.facebook.memory.MemoryConstants;
import com.facebook.memory.UnsafeAccessor;

public enum FieldType {
  INT(Integer.BYTES),
  ADDRESS(MemoryConstants.ADDRESS_SIZE),
  BYTE_ARRAY(Integer.BYTES, address ->
  {
    return UnsafeAccessor.get().getInt(address) + Integer.BYTES;
  }
  )
  ,;

  private final int staticFieldsSize;
  private final FieldSizeFunction fieldSizeFunction;

  FieldType(int staticFieldsSize, FieldSizeFunction fieldSizeFunction) {
    this.staticFieldsSize = staticFieldsSize;
    this.fieldSizeFunction = fieldSizeFunction;
  }

  FieldType(int size) {
    this(size, new FixedFieldSizeFunction(size));
  }

  public FieldSizeFunction getFieldSizeFunction() {
    return fieldSizeFunction;
  }

  public int getStaticFieldsSize() {
    return staticFieldsSize;
  }
}
