package com.facebook.collections.bytearray;

public interface ByteArray extends Comparable<ByteArray>, Iterable<Byte> {
  int getLength();

  byte getAdjusted(int pos);

  void putAdjusted(int pos, byte b);

  boolean isNull();
}
