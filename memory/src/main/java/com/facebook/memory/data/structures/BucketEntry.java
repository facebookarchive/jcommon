package com.facebook.memory.data.structures;

import com.facebook.memory.data.types.definitions.SizedOffHeapStructure;

/**
 * Bucket in a hashmap. This is used to store elements in a map that collide
 */

public interface BucketEntry extends OffHeap {
  SizedOffHeapStructure getKey();
  AnnotatedOffHeapValue getAnnotatedValue();

  void remove();
}
