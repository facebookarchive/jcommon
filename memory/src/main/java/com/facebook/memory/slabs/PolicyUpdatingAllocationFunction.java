package com.facebook.memory.slabs;

import com.facebook.memory.FailedAllocationException;

/**
 * this class is used primarily in the DualAllocationPolicy class. It is used to intercept allocation calls that
 * go to one slab and make sure the other slab receives them as well.
 */
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
    long address = allocationFunction.allocateOn(slab);

    allocationPolicy.updateSlab(slab);

    return address;
  }
}
