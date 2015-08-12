package com.facebook.memory.data.structures;

import com.facebook.memory.FailedAllocationException;
import com.facebook.memory.data.types.definitions.Structs;
import com.facebook.memory.slabs.Slab;

public class OffHeapCacheEntryFactory implements CacheEntryFactory {
  private final Slab slab;

  public OffHeapCacheEntryFactory(Slab slab) {
    this.slab = slab;
  }

  @Override
  public OffHeapCacheEntry create(OffHeap entryAddress) throws FailedAllocationException {
    long address = Structs.allocate(OffHeapCacheEntry.class, slab);
    OffHeapCacheEntry cacheEntry = new OffHeapCacheEntry(address)
      .setDataPointer(entryAddress.getAddress());

    return cacheEntry;
  }
}
