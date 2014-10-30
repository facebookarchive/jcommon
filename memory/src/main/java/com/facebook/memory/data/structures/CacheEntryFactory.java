package com.facebook.memory.data.structures;

import com.facebook.memory.FailedAllocationException;

public interface CacheEntryFactory {
  OffHeapCacheEntry create(OffHeap entryAddress) throws FailedAllocationException;
}
