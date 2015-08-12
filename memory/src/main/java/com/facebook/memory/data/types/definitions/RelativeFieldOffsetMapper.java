package com.facebook.memory.data.types.definitions;

public class RelativeFieldOffsetMapper implements FieldOffsetMapper {
  private final FieldOffsetMapper fieldOffsetMapper;
  private final FieldSizeFunction fieldSizeFunction;

  public RelativeFieldOffsetMapper(
    FieldOffsetMapper previousFieldOffsetMapper,
    FieldSizeFunction fieldSizeFunction
  ) {
    this.fieldOffsetMapper = previousFieldOffsetMapper;
    this.fieldSizeFunction = fieldSizeFunction;
  }

  public RelativeFieldOffsetMapper(FieldSizeFunction fieldSizeFunction) {
    this(new NullFieldOffsetMapper(), fieldSizeFunction);
  }

  @Override
  public int getFieldStartOffset(long address) {
    int fieldStart = fieldOffsetMapper.getFieldStartOffset(address);
    int fieldSize = fieldOffsetMapper.getFieldSize(address);

    return fieldStart + fieldSize;
  }

  @Override
  public int getFieldSize(long address) {
    return getFieldSize(address, getFieldStartOffset(address));
  }

  @Override
  public int getFieldSize(long address, int fieldStartOffset) {
    return fieldSizeFunction.getSize(address + fieldStartOffset);
  }

  private static class NullFieldOffsetMapper implements FieldOffsetMapper {
    @Override
    public int getFieldStartOffset(long address) {
      return 0;
    }

    @Override
    public int getFieldSize(long address) {
      return 0;
    }

    @Override
    public int getFieldSize(long address, int startOffset) {
      return 0;
    }
  }
}
