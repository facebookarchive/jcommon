package com.facebook.memory.data.structures;

public class AnnotatedOffHeapByteArray extends AnnotatedByteArrayBase<OffHeapByteArray> {
  public AnnotatedOffHeapByteArray(long address, long annotationAddress, OffHeapByteArray data) {
    super(address, annotationAddress, data);
  }
}
