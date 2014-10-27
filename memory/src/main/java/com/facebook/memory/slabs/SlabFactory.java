package com.facebook.memory.slabs;

public interface SlabFactory {
  public Slab create(int sizeBytes);
}
