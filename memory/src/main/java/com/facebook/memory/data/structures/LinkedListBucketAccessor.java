package com.facebook.memory.data.structures;

import com.facebook.memory.FailedAllocationException;
import com.facebook.memory.slabs.Slab;
import com.facebook.memory.views.MemoryViewFactory;

public class LinkedListBucketAccessor implements BucketAccessor {
  private final Slab slab;
  private final MemoryViewFactory memoryViewFactory;
  private final SizedOffHeapWrapper keyWrapper;
  private final SizedOffHeapWrapper valueWrapper;

  /**
   *
   * @param slab
   * @param memoryViewFactory
   * @param keyWrapper - eg OffHeapByteArrayImpl::wrap which knows how to interpret bytes at given address
   * @param valueWrapper - same as keyWrapper, but for the value
   */
  public LinkedListBucketAccessor(
    Slab slab, MemoryViewFactory memoryViewFactory, SizedOffHeapWrapper keyWrapper, SizedOffHeapWrapper valueWrapper
  ) {
    this.keyWrapper = keyWrapper;
    this.valueWrapper = valueWrapper;
    this.memoryViewFactory = memoryViewFactory;
    this.slab = slab;
  }

  @Override
  public Bucket create() throws FailedAllocationException {
    return LinkedListBucket.create(slab, memoryViewFactory, keyWrapper, valueWrapper);
  }

  @Override
  public Bucket wrap(long address) {
    return LinkedListBucket.wrap(address, slab, memoryViewFactory, keyWrapper, valueWrapper);
  }
}
