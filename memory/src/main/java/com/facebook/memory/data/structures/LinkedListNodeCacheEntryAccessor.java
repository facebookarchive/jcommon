package com.facebook.memory.data.structures;

import com.facebook.memory.FailedAllocationException;

/**
 * this is a CacheEntryAccessor that understands an overlay of a doubly linked list on the nodes of a LinkedListBucket
 * LinkedListBucketNodeWithLru
 *
 */
public class LinkedListNodeCacheEntryAccessor implements CacheEntryAccessor {
  @Override
  public OffHeapCacheEntry create(OffHeap entryAddress) throws FailedAllocationException {
    // we know we've already got a LinkedListBucketNode and this just wraps it for access
    return new LinkedListNodeCacheEntry(entryAddress);
  }

  @Override
  public OffHeapCacheEntry wrap(long entryAddress) {
    return new LinkedListNodeCacheEntry(entryAddress);
  }

  @Override
  public void free(long entryAddress) {
    // no-op -- since the node is owned by the LinkedListBucket, it handles freeing of the memory
  }
}
