package com.facebook.memory.data.structures;

import com.facebook.memory.FailedAllocationException;
import com.facebook.memory.MemoryConstants;

public class NoOpCachePolicy implements CachePolicy {
  @Override
  public CacheAction addEntry(OffHeap entryAddress, boolean shouldEvict) throws FailedAllocationException {
    // return a bogus annotation address of the same as what passed in;
    return new CacheAction(new CachePolicyKey(entryAddress.getAddress()), false, MemoryConstants.NO_ADDRESS);
  }

  @Override
  public CacheAction updateEntry(CachePolicyKey policyKey, boolean shouldEvict) {
    return new CacheAction(policyKey, false, MemoryConstants.NO_ADDRESS);
  }

  @Override
  public CacheAction removeEntry(CachePolicyKey policyKey, boolean shouldEvict) {
    return new CacheAction(policyKey, false, MemoryConstants.NO_ADDRESS);
  }
}
