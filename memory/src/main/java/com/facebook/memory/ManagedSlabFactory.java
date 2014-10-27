package com.facebook.memory;

import com.facebook.memory.slabs.Slab;
import com.facebook.memory.slabs.SlabFactory;
import com.facebook.memory.slabs.Slabs;

public class ManagedSlabFactory implements SlabFactory {
  @Override
  public Slab create(int sizeBytes) {
    return Slabs.newManagedSlab(sizeBytes);
  }
}
