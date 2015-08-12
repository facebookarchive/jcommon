package com.facebook.memory.data.structures;

import com.facebook.memory.FailedAllocationException;
import com.facebook.memory.slabs.Slab;

public class OffHeapByteArrayAccessorImpl implements OffHeapByteArrayAccessor {
  private final Slab slab;

  public OffHeapByteArrayAccessorImpl(Slab slab) {
    this.slab = slab;
  }

  @Override
  public OffHeapByteArray create(int size) throws FailedAllocationException {
    return OffHeapByteArrayImpl.allocate(size, slab);
  }

  @Override
  public OffHeapByteArray wrap(long address) {
    return OffHeapByteArrayImpl.wrap(address);
  }
}
