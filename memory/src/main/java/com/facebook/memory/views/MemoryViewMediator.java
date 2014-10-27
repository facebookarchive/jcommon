package com.facebook.memory.views;

import com.facebook.memory.FailedAllocationException;
import com.facebook.memory.slabs.Slab;

public class MemoryViewMediator {
  private final Slab slab;

  public MemoryViewMediator(Slab slab) {
    this.slab = slab;
  }

  public static MemoryViewMediator fromSlab(Slab slab) {
    return new MemoryViewMediator(slab);
  }

  public MemoryView allocate64(long size) throws FailedAllocationException {
    long address = slab.allocate(size);
    MemoryView memoryView = MemoryView64.factory()
      .create(address, size);

    return memoryView;
  }

  public MemoryView allocate32(int size) throws FailedAllocationException {
    long address = slab.allocate(size);
    MemoryView memoryView = MemoryView32.factory()
      .create(address, size);

    return memoryView;
  }

  public MemoryView allocate16(short size) throws FailedAllocationException {
    long address = slab.allocate(size);
    MemoryView memoryView = MemoryView16.factory()
      .create(address, size);

    return memoryView;
  }

  public MemoryView allocateHeap(int size) {
    MemoryView memoryView = HeapMemoryView.factory().create(size);

    return memoryView;
  }

  public MemoryView reset(MemoryView memoryView) {
    if (memoryView.getMaxSize() == Long.MAX_VALUE) {
      MemoryView resetCopyMemoryView = MemoryView64.factory()
        .create((MemoryView64) memoryView);

      return resetCopyMemoryView;
    } else if (memoryView.getMaxSize() == Integer.MAX_VALUE) {
      MemoryView resetCopyMemoryView = MemoryView32.factory()
        .create((MemoryView32) memoryView);

      return resetCopyMemoryView;
    } else if (memoryView.getMaxSize() == Short.MAX_VALUE) {
      MemoryView resetCopyMemoryView = MemoryView16.factory()
        .create((MemoryView16) memoryView);

      return resetCopyMemoryView;
    } else {
      MemoryView resetCopyMemoryView = HeapMemoryView.factory()
        .create((HeapMemoryView) memoryView);

      return resetCopyMemoryView;
    }
  }
}
