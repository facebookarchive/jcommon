package com.facebook.memory.views;

import com.facebook.memory.FailedAllocationException;
import com.facebook.memory.slabs.Slab;

public class MemoryViewAccessor {
  private final MemoryViewFactory memoryViewFactory;
  private final Slab slab;

  public MemoryViewAccessor(MemoryViewFactory memoryViewFactory, Slab slab) {
    this.memoryViewFactory = memoryViewFactory;
    this.slab = slab;
  }

  public MemoryView allocate(long size) throws FailedAllocationException {
    long address = slab.allocate(size);

    return memoryViewFactory.wrap(address, size);
  }
}
