package com.facebook.memory.data.structures;

import com.facebook.memory.data.types.definitions.SizedOffHeapStructure;

public interface EvictionFunction {
  boolean shouldEvictOnNewEntry(SizedOffHeapStructure key, SizedOffHeapStructure value, OffHeapMap<?, ?> offHeapMap);

  boolean shouldEvictOnUpdate(
    SizedOffHeapStructure oldValue,
    SizedOffHeapStructure newValue,
    OffHeapMap<?, ?> offHeapMap
  );

  boolean shouldEvict(OffHeapMap<?, ?> offHeapMap);
}
