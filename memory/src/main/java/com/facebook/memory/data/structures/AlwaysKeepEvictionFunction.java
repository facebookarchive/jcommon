package com.facebook.memory.data.structures;

import com.facebook.memory.data.types.definitions.SizedOffHeapStructure;

public class AlwaysKeepEvictionFunction implements EvictionFunction {
  @Override
  public boolean shouldEvictOnNewEntry(
    SizedOffHeapStructure key, SizedOffHeapStructure value, OffHeapMap<?, ?> offHeapMap
  ) {
    return false;
  }

  @Override
  public boolean shouldEvictOnUpdate(
    SizedOffHeapStructure oldValue, SizedOffHeapStructure newValue, OffHeapMap<?, ?> offHeapMap
  ) {
    return false;
  }

  @Override
  public boolean shouldEvict(OffHeapMap<?, ?> offHeapMap) {
    return false;
  }
}
