package com.facebook.memory.slabs;

import com.google.common.base.Preconditions;

import com.facebook.config.ConfigUtil;
import com.facebook.memory.AllocationContext;
import com.facebook.memory.ManagedSlabFactory;

public class Slabs {

  private Slabs() {
    throw new AssertionError();
  }

  public static int validateSize(long sizeBytes) {
    Preconditions.checkArgument(sizeBytes < Integer.MAX_VALUE);

    return (int) sizeBytes;
  }

  public static Slab synchronizedSlab(Slab slab) {
    return new SynchronizedSlab(slab);
  }

  public static Slab fromRaw(RawSlab slab) {
    return new RawSlabAdapter(slab);
  }

  public static Slab newManagedSlab(int sizeBytes) {
    RawSlab rawSlab = new OffHeapSlab(sizeBytes);

    return new ManagedSlab(rawSlab.getBaseAddress(), rawSlab, sizeBytes);
  }

  public static RawSlab newRawSlab(long sizeBytes) {
    RawSlab rawSlab = new OffHeapSlab(sizeBytes);

    return rawSlab;
  }

  public static SlabFactory managedSlabFactory() {
    return new ManagedSlabFactory();
  }

  public static ShardedSlabPool shardedSlabPool(SlabFactory slabFactory, int numSlabs, int slabSizeBytes) {
    return ShardedSlabPool.create(slabFactory, numSlabs, slabSizeBytes);
  }

  public static long toLongBytes(String sizeWithUnits) {
    return ConfigUtil.getSizeBytes(sizeWithUnits);
  }

  public static int toIntBytes(String sizeWithUnits) {
    return (int) ConfigUtil.getSizeBytes(sizeWithUnits);
  }

  public static AllocationContext threadLocalAllocation() {
    return new AllocationContext(Thread.currentThread().getId());
  }
}
