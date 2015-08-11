package com.facebook.memory.slabs;

public class ManagedSlabFactory implements SlabFactory {
  @Override
  public Slab create(int sizeBytes) {
    return Slabs.newManagedSlab(sizeBytes);
  }
}
