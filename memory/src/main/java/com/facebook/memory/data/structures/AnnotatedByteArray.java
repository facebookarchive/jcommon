package com.facebook.memory.data.structures;

public class AnnotatedByteArray implements OffHeap, Annotated {
  private final long address;
  private final long annotationAddress;
  private volatile byte[] data;

  public AnnotatedByteArray(long address, long annotationAddress, byte[] data) {
    this.address = address;
    this.annotationAddress = annotationAddress;
    this.data = data;
  }

  public AnnotatedByteArray(long address, long annotationAddress) {
    this(address, annotationAddress, null);
  }

  public AnnotatedByteArray setData(byte[] data) {
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

  public byte[] getByteArray() {
    return data;
  }
}
