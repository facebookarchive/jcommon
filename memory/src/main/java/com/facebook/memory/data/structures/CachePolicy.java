package com.facebook.memory.data.structures;

import com.facebook.memory.FailedAllocationException;

public interface CachePolicy {
  CacheAction addEntry(OffHeap entryAddress) throws FailedAllocationException;

  CacheAction updateEntry(CachePolicyKey policyKey);

  CacheAction removeEntry(CachePolicyKey policyKey);
}
