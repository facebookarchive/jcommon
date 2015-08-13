package com.facebook.memory.data.types.definitions;

public class WrappedFieldOffsetMapper implements FieldOffsetMapper {
  private final FieldOffsetMapper fieldOffsetMapper;

  public WrappedFieldOffsetMapper(FieldOffsetMapper fieldOffsetMapper) {
    this.fieldOffsetMapper = fieldOffsetMapper;
  }

  @Override
  public int getFieldStartOffset(long address) {return fieldOffsetMapper.getFieldStartOffset(address);}

  @Override
  public int getFieldSize(long address) {return fieldOffsetMapper.getFieldSize(address);}

  @Override
  public int getFieldSize(long address, int startOffset) {return fieldOffsetMapper.getFieldSize(address, startOffset);}
}
