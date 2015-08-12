package com.facebook.memory.data.structures;

import com.google.common.base.Preconditions;

import com.facebook.memory.data.types.definitions.SizedOffHeapStructure;

public class MaxMapSizeEvictionFunction implements EvictionFunction {
  private final long maxSize;

  public MaxMapSizeEvictionFunction(long maxSize) {
    Preconditions.checkArgument(maxSize > 0, "max size must be > 0 [%s]", maxSize);
    this.maxSize = maxSize;
  }

  @Override
  public boolean shouldEvictOnUpdate(
    SizedOffHeapStructure oldValue, SizedOffHeapStructure newValue, OffHeapMap<?, ?> offHeapMap
  ) {
    return offHeapMap.getSize() - oldValue.getSize() + newValue.getSize() > maxSize;
  }

  @Override
  public boolean shouldEvictOnNewEntry(
    SizedOffHeapStructure key, SizedOffHeapStructure value, OffHeapMap<?, ?> offHeapMap
  ) {
    return offHeapMap.getSize() + key.getSize() + value.getSize() > maxSize;
  }

  @Override
  public boolean shouldEvict(OffHeapMap<?, ?> offHeapMap) {
    return offHeapMap.getSize() > maxSize;
  }
}
