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
  public long allocateOn(Slab slab) throws FailedAllocationException {
    Long address = allocationFunction.allocateOn(slab);
    allocationPolicy.updateSlab(slab);

    return address;
  }
}
