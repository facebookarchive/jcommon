package com.facebook.collections.bytearray;

import java.util.NoSuchElementException;

class NullByteArray extends AbstractByteArray {
  @Override
  public int getLength() {
    return 0;
  }

  @Override
  public byte getAdjusted(int pos) {
    throw new NoSuchElementException("null ByteArray");
  }

  @Override
  public void putAdjusted(int pos, byte b) {
    throw new UnsupportedOperationException("cannot write to a null byte array");
  }

  @Override
  public boolean isNull() {
    return true;
  }

  @Override
  public int compareTo(ByteArray o) {
    // order is null, NullByteArray, [all others]
    if (o == null) {
      return 1;
    } else {
      return o.isNull() ? 0 : -1;
    }
  }
}
