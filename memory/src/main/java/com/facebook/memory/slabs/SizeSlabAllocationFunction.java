package com.facebook.memory.slabs;

import com.facebook.memory.FailedAllocationException;

public class SizeSlabAllocationFunction implements SlabAllocationFunction {
  private final long sizeBytes;

  public SizeSlabAllocationFunction(long sizeBytes) {
    this.sizeBytes = sizeBytes;
  }

  @Override
  public Long execute(Slab slab) throws FailedAllocationException {
    return slab.allocate(sizeBytes);
  }
}
