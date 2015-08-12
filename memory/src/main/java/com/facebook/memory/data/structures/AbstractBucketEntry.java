package com.facebook.memory.data.structures;

import com.facebook.memory.data.types.definitions.SizedOffHeapStructure;

public abstract class AbstractBucketEntry implements BucketEntry {
  private final SizedOffHeapStructure key;
  private final AnnotatedOffHeapValue value;
  private final long address;

  public AbstractBucketEntry(SizedOffHeapStructure key, AnnotatedOffHeapValue value, long address) {
    this.key = key;
    this.value = value;
    this.address = address;
  }

  @Override
  public SizedOffHeapStructure getKey() {
    return key;
  }

  @Override
  public AnnotatedOffHeapValue getAnnotatedValue() {
    return value;
  }

  @Override
  public long getAddress() {
    return address;
  }
}
