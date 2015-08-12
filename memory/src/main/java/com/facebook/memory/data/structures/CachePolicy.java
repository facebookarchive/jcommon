package com.facebook.memory.data.structures;

import com.facebook.memory.FailedAllocationException;

/**
 * The CachePolicy primarily tells *what* will be evicted, if requested
 *
 * on any cache mutation (add, remove, update), the CachePolicy is notified. It is told what entry was updated, and if
 * it should perform an eviction.
 */
public interface CachePolicy {
  /**
   * called when a cache entry is written to
   * @param entryAddress
   * @return
   * @throws FailedAllocationException
   */
  CacheAction addEntry(OffHeap entryAddress, boolean shouldEvict) throws FailedAllocationException;

  /**
   * called when a cache entry is read
   * @param policyKey
   * @return
   */
  CacheAction updateEntry(CachePolicyKey policyKey, boolean shouldEvict);

  /**
   * called when an entry is removed
   *
   * @param policyKey
   * @return
   */
  CacheAction removeEntry(CachePolicyKey policyKey, boolean shouldEvict);
}
