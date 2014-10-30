package com.facebook.memory.data.structures;

import com.facebook.memory.FailedAllocationException;

public interface CachePolicy {
  CachePolicyKey addEntry(OffHeap entryAddress) throws FailedAllocationException;

  void updateEntry(CachePolicyKey policyKey);

  void removeEntry(CachePolicyKey  policyKey);

  boolean shouldEvict();

  long getTokenToRemove();
}
