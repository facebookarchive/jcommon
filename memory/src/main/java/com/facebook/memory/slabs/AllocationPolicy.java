package com.facebook.memory.slabs;

import com.facebook.memory.FailedAllocationException;

public interface AllocationPolicy {
  long allocate(SlabAllocationFunction slabAllocationFunction) throws FailedAllocationException;

  void updateSlab(Slab slab);
}
