package com.facebook.memory.lifecycle;

import com.google.common.base.Preconditions;

import com.facebook.collections.CounterMap;
import com.facebook.memory.data.types.definitions.SizedOffHeapStructure;
import com.facebook.memory.slabs.Slab;

/**
 * This class tracks java-heap references to off-heap addresses. The idea is basic reference counting and allows for
 * objects to be reclaimed when there are no more references.
 */
public class ReferenceManager {
  private final Slab slab;
  private final CounterMap<Long> referenceCount;

  public ReferenceManager(Slab slab, CounterMap<Long> referenceCount) {
    this.slab = slab;
    this.referenceCount = referenceCount;
  }

  public ReferenceManager(Slab slab) {
    this(slab, new CounterMap<>());
  }

  /**
   * @param offHeapStructure item to increment count for
   * @return ref count after increment
   */
  public long increment(SizedOffHeapStructure offHeapStructure) {
    return referenceCount.addAndGet(offHeapStructure.getAddress(), 1);
  }

  /**
   *
   * @param offHeapStructure item to decrement count for
   * @return ref counter after decrement (should never be negative)
   */
  public long decrement(SizedOffHeapStructure offHeapStructure) {
    long value = referenceCount.addAndGet(offHeapStructure.getAddress(), -1);

    Preconditions.checkState(value >= 0, "negative reference count %s", value);

    if (value == 0) {
      slab.free(offHeapStructure.getAddress(), offHeapStructure.getSize());
    }

    return value;
  }
}
