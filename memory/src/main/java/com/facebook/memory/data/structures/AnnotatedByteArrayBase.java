package com.facebook.memory.data.structures;

import com.facebook.collections.bytearray.ByteArray;

public class AnnotatedByteArrayBase<T extends ByteArray> implements AnnotatedOffHeap {
  private final long address;
  private final long annotationAddress;
  private volatile T data;

  public AnnotatedByteArrayBase(long address, long annotationAddress, T data) {
    this.address = address;
    this.annotationAddress = annotationAddress;
    this.data = data;
  }

  public AnnotatedByteArrayBase setData(T data) {
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

  public T getByteArray() {
    return data;
  }
}
