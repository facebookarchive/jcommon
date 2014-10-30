package com.facebook.memory.views;

import com.facebook.memory.FailedAllocationException;
import com.facebook.memory.slabs.Slab;

public class MemoryViewController implements MemoryViewFactory {
  private final MemoryViewFactory memoryViewFactory;
  private final Slab slab;

  public MemoryViewController(MemoryViewFactory memoryViewFactory, Slab slab) {
    this.memoryViewFactory = memoryViewFactory;
    this.slab = slab;
  }

  public MemoryView allocate(long size) throws FailedAllocationException {
    long address = slab.allocate(size);

    return memoryViewFactory.wrap(address, size);
  }

  public void free(long address, int size) {
    slab.free(address,size);
  }

  @Override
  public MemoryView wrap(long address, long size) {return memoryViewFactory.wrap(address, size);}

  @Override
  public MemoryView wrap(ReadableMemoryView memoryView) {return memoryViewFactory.wrap(memoryView);}

  @Override
  public ReadableMemoryView wrapByte(long address) {return memoryViewFactory.wrapByte(address);}

  @Override
  public ReadableMemoryView wrapShort(long address) {return memoryViewFactory.wrapShort(address);}

  @Override
  public ReadableMemoryView wrapInt(long address) {return memoryViewFactory.wrapInt(address);}

  @Override
  public ReadableMemoryView wrapLong(long address) {return memoryViewFactory.wrapLong(address);}
}
