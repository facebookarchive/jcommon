package com.facebook.memory.data.structures;

import com.facebook.memory.views.MemoryViewFactory;

public class LinkedListKeyWrapper implements OffHeapByteArrayWrapper {
  private final MemoryViewFactory memoryViewFactory;

  public LinkedListKeyWrapper(MemoryViewFactory memoryViewFactory) {
    this.memoryViewFactory = memoryViewFactory;
  }

  @Override
  public OffHeapByteArray wrap(long address) {
    return LinkedListNode.wrap(address, memoryViewFactory).getKey();
  }
}
