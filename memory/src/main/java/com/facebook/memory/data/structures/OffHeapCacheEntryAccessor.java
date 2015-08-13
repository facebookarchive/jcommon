package com.facebook.memory.data.structures;

import com.facebook.memory.FailedAllocationException;
import com.facebook.memory.slabs.Slab;

public class OffHeapCacheEntryAccessor implements CacheEntryAccessor {
  private final Slab slab;

  public OffHeapCacheEntryAccessor(Slab slab) {
    this.slab = slab;
  }

  @Override
  public OffHeapCacheEntry create(OffHeap entryAddress) throws FailedAllocationException {
    OffHeapCacheEntry cacheEntry = OffHeapCacheEntryImpl.allocate(slab);

    cacheEntry.setDataPointer(entryAddress.getAddress());

    return cacheEntry;
  }

  @Override
  public OffHeapCacheEntry wrap(long entryAddress) {
    return OffHeapCacheEntryImpl.wrap(entryAddress);
  }

  @Override
  public void free(long entryAddress) {
    slab.free(entryAddress, OffHeapCacheEntryImpl.SIZE);
  }
}
