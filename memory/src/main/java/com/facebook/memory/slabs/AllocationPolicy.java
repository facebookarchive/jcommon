package com.facebook.memory.slabs;

import com.facebook.memory.FailedAllocationException;

public interface AllocationPolicy {
  long allocate(SlabAllocationFunction allocationFunction) throws FailedAllocationException;

  Allocation tryAllocate(SlabTryAllocationFunction tryAllocationFunction);

  void updateSlab(Slab slab);
}
