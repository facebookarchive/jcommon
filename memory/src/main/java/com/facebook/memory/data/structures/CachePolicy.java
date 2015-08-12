package com.facebook.memory.data.structures;

import com.facebook.memory.FailedAllocationException;

/**
 * on any cache mutation (add, remove, update), the CachePolicy is notified. It can then indicate if
 * a token should be removed in the CacheAction
 */
public interface CachePolicy {
  /**
   * called when a cache entry is written to
   * @param entryAddress
   * @return
   * @throws FailedAllocationException
   */
  CacheAction addEntry(OffHeap entryAddress) throws FailedAllocationException;

  /**
   * called when a cache entry is read
   * @param policyKey
   * @return
   */
  CacheAction updateEntry(CachePolicyKey policyKey);

  /**
   * called when an entry is removed
   *
   * @param policyKey
   * @return
   */
  CacheAction removeEntry(CachePolicyKey policyKey);
}
