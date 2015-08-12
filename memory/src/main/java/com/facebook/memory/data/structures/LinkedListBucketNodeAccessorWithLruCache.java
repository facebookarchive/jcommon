package com.facebook.memory.data.structures;

import com.facebook.memory.FailedAllocationException;
import com.facebook.memory.data.types.definitions.SizedOffHeapStructure;
import com.facebook.memory.slabs.Slab;

public class LinkedListBucketNodeAccessorWithLruCache implements LinkedListBucketNodeAccessor {
  @Override
  public LinkedListBucketNodeWithLruCache wrap(
    long address, SizedOffHeapWrapper keyWrapper, SizedOffHeapWrapper valueWrapper
  ) {
    return LinkedListBucketNodeWithLruCache.wrap(address, keyWrapper, valueWrapper);
  }

  @Override
  public LinkedListBucketNodeWithLruCache create(
    Slab slab,
    SizedOffHeapStructure key,
    SizedOffHeapStructure value,
    SizedOffHeapWrapper keyWrapper,
    SizedOffHeapWrapper valueWrapper
  ) throws FailedAllocationException {
    return LinkedListBucketNodeWithLruCache.create(slab, key, value, keyWrapper, valueWrapper);
  }
}
