package com.facebook.collections.bytearray;

public class WrappedByteArray extends AbstractByteArray {
  private final ByteArray byteArray;

  public WrappedByteArray(ByteArray byteArray) {
    this.byteArray = byteArray;
  }

  @Override
  public int getLength() {return byteArray.getLength();}

  @Override
  public byte getAdjusted(int pos) {return byteArray.getAdjusted(pos);}

  @Override
  public void putAdjusted(int pos, byte b) {byteArray.putAdjusted(pos, b);}

  @Override
  public boolean isNull() {return byteArray.isNull();}
}
