package com.facebook.memory.slabs;

import com.facebook.memory.FailedAllocationException;

public class MemoryStatsSlab extends WrappedSlab {
  public MemoryStatsSlab(Slab slab) {
    super(slab);
  }

  @Override
  public long allocate(long sizeBytes) throws FailedAllocationException {
    return super.allocate(sizeBytes);
  }

  @Override
  public void free(long address, int size)  {
    super.free(address, size);
  }
}
