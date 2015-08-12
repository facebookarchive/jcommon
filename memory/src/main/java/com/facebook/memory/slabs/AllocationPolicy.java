package com.facebook.memory.slabs;

import com.facebook.memory.FailedAllocationException;

public interface AllocationPolicy {
  /**
   * allocate
   * @param allocationFunction
   * @return
   * @throws FailedAllocationException
   */
  long allocate(SlabAllocationFunction allocationFunction) throws FailedAllocationException;

  Allocation tryAllocate(SlabTryAllocationFunction tryAllocationFunction);

  /**
   * this function should be called whenever the caller knows that the slab size may have changed. This may be
   * to inform of both allocations done outside the policy, or free operations performed.
   * @param slab
   */
  void updateSlab(Slab slab);
}
