package com.facebook.memory.slabs;

import com.google.common.base.Preconditions;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.facebook.config.ConfigUtil;

public class Slabs {

  private Slabs() {
    throw new AssertionError();
  }

  public static int validateSize(long sizeBytes) {
    Preconditions.checkArgument(sizeBytes < Integer.MAX_VALUE && sizeBytes >= Integer.MIN_VALUE);

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

  /**
   * in a thread-safe manner, will add delta to an AtomicInteger up to a max.  If the max is exceeded, the final
   * amount added is recorded and returned
   *
   * eg:  max = 100, bytesUsed = 95, delta = 10
   *
   * this will return with bytesUsed = 100, and return the value 5
   *
   * note: this is concurrent-safe in that if multiple threads do this conurrently, on may simply return 0
   * @param baseAddress
   * @param bytesUsed
   * @param delta
   * @param maxValue
   * @return
   */
  public static AddWithMaxResult allocateFromAtomicInteger(
    AtomicInteger bytesUsed,
    int delta,
    int maxValue
  ) {
    int validatedSizeBytes = Slabs.validateSize(delta);
    int preAllocatedValue = bytesUsed.getAndAdd(validatedSizeBytes);
    int postAllocationSize = validatedSizeBytes + preAllocatedValue;

    int bytesAllocated;

    if (postAllocationSize > maxValue) {
      bytesAllocated = maxValue - preAllocatedValue;
      // restore the reserved bytes that aren't used
      bytesUsed.addAndGet(validatedSizeBytes - bytesAllocated);
    } else {
      bytesAllocated = validatedSizeBytes;
    }
    return new AddWithMaxResult(preAllocatedValue, Math.max(bytesAllocated, 0));
  }

  @SuppressWarnings("NumericCastThatLosesPrecision")
  public static AddWithMaxResult allocateFromAtomicLong(
    AtomicLong bytesUsed,
    int delta,
    long maxValue
  ) {
    int validatedSizeBytes = Slabs.validateSize(delta);
    long preAllocatedValue = bytesUsed.getAndAdd(validatedSizeBytes);
    long postAllocationSize = validatedSizeBytes + preAllocatedValue;

    int bytesAllocated;

    if (postAllocationSize > maxValue) {
      // sinze delta is an int, we know this is within an integer
      bytesAllocated = (int)(maxValue - preAllocatedValue);
      // restore the reserved bytes that aren't used
      bytesUsed.addAndGet(validatedSizeBytes - bytesAllocated);
    } else {
      bytesAllocated = validatedSizeBytes;
    }

    return new AddWithMaxResult(preAllocatedValue, Math.max(bytesAllocated, 0));
  }
}
