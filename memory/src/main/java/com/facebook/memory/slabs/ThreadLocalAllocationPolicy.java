package com.facebook.memory.slabs;

import net.jpountz.xxhash.XXHash32;
import net.jpountz.xxhash.XXHashFactory;

import com.facebook.memory.FailedAllocationException;
import com.facebook.util.digest.LongMurmur3Hash;

public class ThreadLocalAllocationPolicy implements AllocationPolicy {
  private static final LongMurmur3Hash MURMUR3_HASH = LongMurmur3Hash.getInstance();
  private static final XXHash32 XX_HASH_32 = XXHashFactory.unsafeInstance().hash32();

  private final ThreadLocal<Slab> tlab;

  public ThreadLocalAllocationPolicy(SlabPool slabPool) {
    tlab = new ThreadLocal<Slab>() {
      @Override
      protected Slab initialValue() {
        long threadId = Thread.currentThread().getId();
        long hashed = MURMUR3_HASH.computeDigest(threadId);
        int index = Math.abs((int) (hashed % slabPool.getSize()));

        Slab slab = slabPool.getSlabByIndex(index);

        return slab;
      }
    };
  }

  @Override
  public Allocation tryAllocate(SlabTryAllocationFunction tryAllocationFunction) {
    Slab slab = internalGetSlab();

    return tryAllocationFunction.tryAllocateOn(slab);
  }

  @Override
  public long allocate(SlabAllocationFunction allocationFunction) throws FailedAllocationException {
    Slab slab = internalGetSlab();

    return allocationFunction.allocateOn(slab);
  }

  @Override
  public void updateSlab(Slab slab) {
    // no-op
  }

  @Override
  public String toString() {
    return getClass().getName();
  }

  private Slab internalGetSlab() {
    return tlab.get();
  }
}
