package com.facebook.memory.data.structures;

import com.google.common.base.MoreObjects;

import com.facebook.memory.FailedAllocationException;
import com.facebook.memory.data.types.DoublyLinkedListNode;
import com.facebook.memory.data.types.definitions.Structs;
import com.facebook.memory.slabs.Slab;

public class OffHeapCacheEntryImpl extends DoublyLinkedListNode implements OffHeapCacheEntry {
  public static final int SIZE = Structs.getStaticSlotSize(OffHeapCacheEntryImpl.class);

  public OffHeapCacheEntryImpl(long address) {
    super(address);
  }

  public static OffHeapCacheEntryImpl wrap(long address) {
    return new OffHeapCacheEntryImpl(address);
  }

  public static OffHeapCacheEntryImpl allocate(Slab slab) throws FailedAllocationException {
    return new OffHeapCacheEntryImpl(slab.allocate(SIZE));
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
