package com.facebook.memory.data.types.definitions;

public class FixedFieldSizeFunction implements FieldSizeFunction {
  private final int size;

  public FixedFieldSizeFunction(int size) {
    this.size = size;
  }

  @Override
  public int getSize(long address) {
    return size;
  }
}
