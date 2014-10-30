package com.facebook.memory.data.types.definitions;

import com.facebook.memory.FailedAllocationException;
import com.facebook.memory.UnsafeAccessor;
import com.facebook.memory.data.types.PointerMorph;
import com.facebook.memory.slabs.Slab;
import com.facebook.memory.views.MemoryView32;
import com.facebook.memory.views.ReadableMemoryView;

public class Node {
  public static final Node DEF = new Node(0);

  private final PointerMorph next;
  private final PointerMorph previous;

  public Node(int offset) {
    next = new PointerMorph("next", offset);
    previous = new PointerMorph("previous", offset + UnsafeAccessor.get().addressSize());
  }

  public PointerMorph next() {
    return next;
  }

  public PointerMorph previous() {
    return previous;
  }

  public int getSize() {
    return next.getSize() + previous.getSize();
  }

  public ReadableMemoryView allocate(Slab slab) throws FailedAllocationException {
    long address = slab.allocate(getSize());

    return MemoryView32.factory().wrap(address, getSize());
  }
}
