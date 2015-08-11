package com.facebook.collections.bytearray;

import java.util.Arrays;

class ByteArrayView extends AbstractByteArray {
  private final byte[] array;
  private final int offset;
  private final int length;

  ByteArrayView(byte[] array, int offset, int length) {
    this.array = array;
    this.offset = offset;
    this.length = length;
  }

  ByteArrayView(byte[] array, int offset) {
    this(array, offset, array.length - offset);
  }

  ByteArrayView(byte[] array) {
    this(array, 0, array.length);
  }

  @Override
  public int getLength() {
    return length;
  }

  @Override
  public byte getAdjusted(int pos) {
    return array[offset + pos];
  }

  @Override
  public void putAdjusted(int pos, byte b) {
    array[offset + pos] = b;
  }

  @Override
  public boolean isNull() {
    return array == null;
  }

  @Override
  public String toString() {
    return "ByteArrayView{" +
      "array=" + Arrays.toString(array) +
      ", start=" + offset +
      ", length=" + length +
      "} " + toString();
  }
}
