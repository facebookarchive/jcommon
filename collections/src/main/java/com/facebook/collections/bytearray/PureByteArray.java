package com.facebook.collections.bytearray;

import java.util.Arrays;

class PureByteArray extends AbstractByteArray {
  private final byte[] array;

  PureByteArray(byte[] array) {
    this.array = array;
  }

  @Override
  public int getLength() {
    return array.length;
  }

  @Override
  public byte getAdjusted(int pos) {
    return array[pos];
  }

  @Override
  public void putAdjusted(int pos, byte b) {
    array[pos] = b;
  }

  @Override
  public boolean isNull() {
    return array == null;
  }

  @Override
  public String toString() {
    return "PureByteArray{" +
      "array=" + Arrays.toString(array) +
      '}';
  }
}
