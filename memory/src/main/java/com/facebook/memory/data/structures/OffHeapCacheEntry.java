package com.facebook.memory.data.structures;

import com.google.common.base.MoreObjects;

import com.facebook.memory.FailedAllocationException;
import com.facebook.memory.data.types.DoublyLinkedListNode;
import com.facebook.memory.data.types.definitions.Structs;
import com.facebook.memory.slabs.Slab;

public class OffHeapCacheEntry extends DoublyLinkedListNode {
  public static final int SIZE = Structs.getStructSize(OffHeapCacheEntry.class);

  public OffHeapCacheEntry(long address) {
    super(address);
  }

  public static OffHeapCacheEntry wrap(long address) {
    return new OffHeapCacheEntry(address);
  }

  public static OffHeapCacheEntry allocate(Slab slab) throws FailedAllocationException {
    return new OffHeapCacheEntry(slab.allocate(SIZE));
  }

  @Override
  public OffHeapCacheEntry setPrevious(long value) {
    super.setPrevious(value);

    return this;
  }

  @Override
  public com.facebook.memory.data.types.LinkedListNode setNext(long value) {
    super.setNext(value);

    return this;

  }

  @Override
  public OffHeapCacheEntry setDataPointer(long value) {
    super.setDataPointer(value);

    return this;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .add("address", getAddress())
      .add("entryAddress", getDataPointer())
      .add("next", getNext())
      .add("previous", getPrevious())
      .toString();
  }
}
