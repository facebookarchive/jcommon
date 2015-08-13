package com.facebook.memory.data.structures;

import com.facebook.collections.bytearray.ByteArray;

public class AnnotatedByteArray implements OffHeap, Annotated {
  private final long address;
  private final long annotationAddress;
  private volatile ByteArray data;

  public AnnotatedByteArray(long address, long annotationAddress, ByteArray data) {
    this.address = address;
    this.annotationAddress = annotationAddress;
    this.data = data;
  }

  public AnnotatedByteArray setData(ByteArray data) {
    this.data = data;

    return this;
  }

  @Override
  public long getAnnotationAddress() {
    return annotationAddress;
  }

  @Override
  public long getAddress() {
    return address;
  }

  public ByteArray getByteArray() {
    return data;
  }
}
