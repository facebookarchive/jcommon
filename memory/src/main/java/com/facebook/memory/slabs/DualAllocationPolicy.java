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
  public long allocate(final SlabAllocationFunction slabAllocationFunction)
    throws FailedAllocationException {
    try {
      long address = firstPolicy.allocate(new PolicyUpdatingAllocationFunction(slabAllocationFunction, secondPolicy));

      if (address == 0) {
        return secondPolicy.allocate(new PolicyUpdatingAllocationFunction(slabAllocationFunction, secondPolicy));
      }

      return address;
    } catch (FailedAllocationException e) {
      LOGGER.info("[allocate] failed on first policy %s, trying second %s", firstPolicy, secondPolicy);
    }

    return secondPolicy.allocate(new PolicyUpdatingAllocationFunction(slabAllocationFunction, secondPolicy));
  }

  @Override
  public Slab getSlab(long sizeBytes) throws FailedAllocationException {
    try {
      return firstPolicy.getSlab(sizeBytes);
    } catch (FailedAllocationException e) {
      LOGGER.info(
        "[slab] failed on first policy %s, trying second %s for %d bytes", firstPolicy, secondPolicy, sizeBytes
      );
    }

    return secondPolicy.getSlab(sizeBytes);
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
