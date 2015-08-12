package com.facebook.memory.slabs;

import com.facebook.logging.Logger;
import com.facebook.logging.LoggerImpl;
import com.facebook.memory.FailedAllocationException;

public class DualAllocationPolicy implements AllocationPolicy {
  private static final Logger LOGGER = LoggerImpl.getClassLogger();

  private final AllocationPolicy firstPolicy;
  private final AllocationPolicy secondPolicy;

  public DualAllocationPolicy(AllocationPolicy firstPolicy, AllocationPolicy secondPolicy) {
    this.firstPolicy = firstPolicy;
    this.secondPolicy = secondPolicy;
  }

  @Override
  public long allocate(final SlabAllocationFunction allocationFunction)
    throws FailedAllocationException {
    try {
      long address = firstPolicy.allocate(new PolicyUpdatingAllocationFunction(allocationFunction, secondPolicy));

      if (address == 0) {
        return secondPolicy.allocate(new PolicyUpdatingAllocationFunction(allocationFunction, secondPolicy));
      }

      return address;
    } catch (FailedAllocationException e) {
      LOGGER.info("[allocate] failed on first policy %s, trying second %s", firstPolicy, secondPolicy);
    }

    return secondPolicy.allocate(new PolicyUpdatingAllocationFunction(allocationFunction, secondPolicy));
  }

  @Override
  public Allocation tryAllocate(SlabTryAllocationFunction tryAllocationFunction) {
    Allocation allocation = firstPolicy.tryAllocate(
      new PolicyUpdatingTryAllocationFunction(
        tryAllocationFunction,
        secondPolicy
      )
    );

    if (allocation.getSize() == 0) {
      return secondPolicy.tryAllocate(new PolicyUpdatingTryAllocationFunction(tryAllocationFunction, secondPolicy));
    }

    return allocation;
  }

  @Override
  public void updateSlab(Slab slab) {
    try {
      firstPolicy.updateSlab(slab);
    } finally {
      secondPolicy.updateSlab(slab);
    }
  }
}
