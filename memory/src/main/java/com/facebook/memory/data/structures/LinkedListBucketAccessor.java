package com.facebook.memory.data.structures;

import com.facebook.memory.FailedAllocationException;
import com.facebook.memory.slabs.Slab;
import com.facebook.memory.views.MemoryViewFactory;

public class LinkedListBucketAccessor implements BucketAccessor {
  private final MemoryViewFactory memoryViewFactory;
  private final Slab slab;

  public LinkedListBucketAccessor(MemoryViewFactory memoryViewFactory, Slab slab) {
    this.memoryViewFactory = memoryViewFactory;
    this.slab = slab;
  }

  @Override
  public Bucket create() throws FailedAllocationException {
    return LinkedListBucket.create(slab, memoryViewFactory);
  }

  @Override
  public Bucket wrap(long address) {
    return LinkedListBucket.wrap(address, slab, memoryViewFactory);
  }
}
