package com.facebook.memory.data.structures;

import com.facebook.memory.FailedAllocationException;
import com.facebook.memory.data.types.definitions.SizedOffHeapStructure;
import com.facebook.memory.slabs.Slab;

public interface LinkedListBucketNodeAccessor {
  LinkedListBucketNode wrap(long address, SizedOffHeapWrapper keyWrapper, SizedOffHeapWrapper valueWrapper);

  LinkedListBucketNode create(
    Slab slab,
    SizedOffHeapStructure key,
    SizedOffHeapStructure value,
    SizedOffHeapWrapper keyWrapper,
    SizedOffHeapWrapper valueWrapper
  ) throws FailedAllocationException;
}
