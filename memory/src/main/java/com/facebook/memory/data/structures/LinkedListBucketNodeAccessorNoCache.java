package com.facebook.memory.data.structures;

import com.facebook.memory.FailedAllocationException;
import com.facebook.memory.data.types.definitions.SizedOffHeapStructure;
import com.facebook.memory.slabs.Slab;

public class LinkedListBucketNodeAccessorNoCache implements LinkedListBucketNodeAccessor {
  @Override
  public LinkedListBucketNode wrap(
    long address, SizedOffHeapWrapper keyWrapper, SizedOffHeapWrapper valueWrapper
  ) {
    return LinkedListBucketNode.wrap(address, keyWrapper, valueWrapper);
  }

  @Override
  public LinkedListBucketNode create(
    Slab slab,
    SizedOffHeapStructure key,
    SizedOffHeapStructure value,
    SizedOffHeapWrapper keyWrapper,
    SizedOffHeapWrapper valueWrapper
  ) throws FailedAllocationException {
    return LinkedListBucketNode.create(slab, key, value, keyWrapper, valueWrapper);
  }
}
