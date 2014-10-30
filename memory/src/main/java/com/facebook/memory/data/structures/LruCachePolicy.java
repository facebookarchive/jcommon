package com.facebook.memory.data.structures;

import com.facebook.memory.FailedAllocationException;
import com.facebook.memory.MemoryConstants;
import com.facebook.memory.views.MemoryViewController;
import com.facebook.util.ExtCallable;

public class LruCachePolicy implements CachePolicy {
  private final CacheEntryFactory cacheEntryFactory;
  private final MemoryViewController memoryViewController;
  private final ExtCallable<Boolean, RuntimeException> shouldEvictFunction;

  private volatile long headNodeAddress = MemoryConstants.NO_ADDRESS;
  private volatile long tailNodeAddress = MemoryConstants.NO_ADDRESS;

  public LruCachePolicy(
    CacheEntryFactory cacheEntryFactory,
    MemoryViewController memoryViewController,
    ExtCallable<Boolean, RuntimeException> shouldEvictFunction
  ) {
    this.cacheEntryFactory = cacheEntryFactory;
    this.memoryViewController = memoryViewController;
    this.shouldEvictFunction = shouldEvictFunction;
  }

  @Override
  public CachePolicyKey addEntry(OffHeap entryAddress) throws FailedAllocationException {
    OffHeapCacheEntry cacheEntry = cacheEntryFactory.create(entryAddress);

    insertAtHead(cacheEntry);

    return new CachePolicyKey(cacheEntry.getAddress());
  }

  private void insertAtHead(OffHeapCacheEntry cacheEntry) {
    cacheEntry.setPrevious(MemoryConstants.NO_ADDRESS);

    if (headNodeAddress == MemoryConstants.NO_ADDRESS) {
      cacheEntry.setNext(MemoryConstants.NO_ADDRESS);
      headNodeAddress = cacheEntry.getAddress();
      tailNodeAddress = cacheEntry.getAddress();
    } else {
      OffHeapCacheEntry headNode = OffHeapCacheEntry.wrap(headNodeAddress);

      headNode.setPrevious(cacheEntry.getAddress());
      cacheEntry.setNext(headNodeAddress);
      headNodeAddress = cacheEntry.getAddress();
    }
  }

  @Override
  public void updateEntry(CachePolicyKey policyKey) {
    OffHeapCacheEntry cacheEntry = OffHeapCacheEntry.wrap(policyKey.getAddress());

    removeStatsNode(cacheEntry);
    insertAtHead(cacheEntry);
  }

  private void removeStatsNode(OffHeapCacheEntry cacheEntry) {
    long previous = cacheEntry.getPrevious();
    long next = cacheEntry.getNext();

    if (headNodeAddress == MemoryConstants.NO_ADDRESS) {
      throw new IllegalStateException("trying to remove with empty list");
    }

    if (previous == MemoryConstants.NO_ADDRESS) {
      headNodeAddress = next;
    } else {
      OffHeapCacheEntry.wrap(previous)
        .setNext(next);
    }

    if (next == MemoryConstants.NO_ADDRESS) {
      tailNodeAddress = previous;
    } else {
      OffHeapCacheEntry.wrap(next)
        .setPrevious(previous);
    }

  }

  @Override
  public void removeEntry(CachePolicyKey policyKey) {
    OffHeapCacheEntry cacheKey = OffHeapCacheEntry.wrap(policyKey.getAddress());

    removeStatsNode(cacheKey);

    memoryViewController.free(policyKey.getAddress(), OffHeapCacheEntry.SIZE);
  }

  @Override
  public boolean shouldEvict() {
    return shouldEvictFunction.call();
  }

  @Override
  public long getTokenToRemove() {
    return tailNodeAddress == MemoryConstants.NO_ADDRESS ?
      MemoryConstants.NO_ADDRESS :
      OffHeapCacheEntry.wrap(tailNodeAddress).getDataPointer();
  }
}
