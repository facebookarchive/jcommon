package com.facebook.memory.data.structures;

import com.facebook.memory.FailedAllocationException;

/**
 * imlementations manage the allocation, creation, and removal of CacheEntry objects
 */
public interface CacheEntryAccessor {
  OffHeapCacheEntry create(OffHeap entryAddress) throws FailedAllocationException;

  OffHeapCacheEntry wrap(long entryAddress);

  default OffHeapCacheEntry wrap(OffHeap entryAddress) { return wrap(entryAddress.getAddress()); }

  void free(long entryAddress);

  default void free(OffHeap entryAddress) { free(entryAddress.getAddress()); }
}
