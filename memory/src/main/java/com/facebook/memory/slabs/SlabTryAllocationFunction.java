package com.facebook.memory.slabs;

public interface SlabTryAllocationFunction {
  Allocation tryAllocateOn(Slab slab);
}
