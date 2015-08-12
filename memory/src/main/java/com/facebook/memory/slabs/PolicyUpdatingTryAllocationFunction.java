package com.facebook.memory.slabs;

public class PolicyUpdatingTryAllocationFunction implements SlabTryAllocationFunction {
  private final SlabTryAllocationFunction tryAllocationFunction;
  private final AllocationPolicy allocationPolicy;

  public PolicyUpdatingTryAllocationFunction(
    SlabTryAllocationFunction tryAllocationFunction, AllocationPolicy allocationPolicy
  ) {
    this.tryAllocationFunction = tryAllocationFunction;
    this.allocationPolicy = allocationPolicy;
  }

  @Override
  public Allocation tryAllocateOn(Slab slab) {
    Allocation allocation = tryAllocationFunction.tryAllocateOn(slab);

    if (allocation.getSize() > 0) {
      allocationPolicy.updateSlab(slab);
    }

    return allocation;
  }
}
