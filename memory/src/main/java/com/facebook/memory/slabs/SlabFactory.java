package com.facebook.memory.slabs;

public interface SlabFactory {
  Slab create(int sizeBytes);
}
