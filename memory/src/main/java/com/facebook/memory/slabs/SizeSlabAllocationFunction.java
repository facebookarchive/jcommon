package com.facebook.memory.slabs;

import com.facebook.memory.FailedAllocationException;

public class SizeSlabAllocationFunction implements SlabAllocationFunction, SlabTryAllocationFunction {
  private final int sizeBytes;

  public SizeSlabAllocationFunction(int sizeBytes) {
    this.sizeBytes = sizeBytes;
  }

  @Override
  public long allocateOn(Slab slab) throws FailedAllocationException {
    return slab.allocate(sizeBytes);
  }

  @Override
  public Allocation tryAllocateOn(Slab slab) {
    return slab.tryAllocate(sizeBytes);
  }
}
