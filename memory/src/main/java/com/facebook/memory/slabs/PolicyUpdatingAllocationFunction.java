package com.facebook.memory.slabs;

import com.facebook.memory.FailedAllocationException;

public class PolicyUpdatingAllocationFunction implements SlabAllocationFunction {
  private final SlabAllocationFunction allocationFunction;
  private final AllocationPolicy allocationPolicy;

  public PolicyUpdatingAllocationFunction(
    SlabAllocationFunction allocationFunction, AllocationPolicy allocationPolicy
  ) {
    this.allocationFunction = allocationFunction;
    this.allocationPolicy = allocationPolicy;
  }

  @Override
  public Long execute(Slab slab) throws FailedAllocationException {
    Long address = allocationFunction.execute(slab);
    allocationPolicy.updateSlab(slab);

    return address;
  }
}
