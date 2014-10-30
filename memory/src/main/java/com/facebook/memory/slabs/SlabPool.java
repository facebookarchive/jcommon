package com.facebook.memory.slabs;

import com.facebook.memory.AllocationContext;

public interface SlabPool extends Iterable<Slab> {
  public Slab getSlab(AllocationContext context);
  public Slab getSlab(long address);
  public void freeSlabPool();
  public int getSize();
}
