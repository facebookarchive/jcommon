package com.facebook.memory.data.structures;

import com.facebook.memory.data.types.definitions.SizedOffHeapStructure;

public interface EvictionCallback {
  default void keyEvicted(SizedOffHeapStructure key) {}

  default void valueEvicted(SizedOffHeapStructure value) {}
}
