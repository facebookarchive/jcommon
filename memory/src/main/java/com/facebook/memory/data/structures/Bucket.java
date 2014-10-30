package com.facebook.memory.data.structures;

public interface Bucket extends OffHeap {
  AnnotatedByteArray get (byte [] key);

  AnnotatableMemoryAddress put(byte[] k, byte[] v);

  boolean remove(byte[] key);

  long size();

  long capacity();
}
