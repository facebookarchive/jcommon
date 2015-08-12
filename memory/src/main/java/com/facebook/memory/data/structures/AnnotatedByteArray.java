package com.facebook.memory.data.structures;

import com.facebook.collections.bytearray.ByteArray;

public class AnnotatedByteArray extends AnnotatedByteArrayBase<ByteArray> {
  public AnnotatedByteArray(long address, long annotationAddress, ByteArray data) {
    super(address, annotationAddress, data);
  }
}
