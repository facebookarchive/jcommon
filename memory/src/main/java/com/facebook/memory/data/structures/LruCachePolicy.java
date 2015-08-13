package com.facebook.memory.data.structures;

import com.facebook.memory.FailedAllocationException;
import com.facebook.memory.MemoryConstants;
import com.facebook.util.ExtCallable;

public class LruCachePolicy implements CachePolicy {
  private final CacheEntryAccessor cacheEntryAccessor;
  private final ExtCallable<Boolean, RuntimeException> shouldEvictFunction;

  private volatile long headNodeAddress = MemoryConstants.NO_ADDRESS;
  private volatile long tailNodeAddress = MemoryConstants.NO_ADDRESS;

  public LruCachePolicy(
    CacheEntryAccessor cacheEntryAccessor,
    ExtCallable<Boolean, RuntimeException> shouldEvictFunction
  ) {
    this.cacheEntryAccessor = cacheEntryAccessor;
    this.shouldEvictFunction = shouldEvictFunction;
  }

  @Override
  public synchronized CacheAction addEntry(OffHeap entryAddress) throws FailedAllocationException {
    OffHeapCacheEntry cacheEntry = cacheEntryAccessor.create(entryAddress);

    insertAtHead(cacheEntry);

    CachePolicyKey cachePolicyKey = new CachePolicyKey(cacheEntry.getAddress());

    return getCacheAction(cachePolicyKey, shouldEvictFunction.call());
  }

  @Override
  public synchronized CacheAction updateEntry(CachePolicyKey policyKey) {
    OffHeapCacheEntry cacheEntry = cacheEntryAccessor.wrap(policyKey);

    if (headNodeAddress != tailNodeAddress) {
      removeStatsNode(cacheEntry);
      insertAtHead(cacheEntry);
    }

    Boolean shouldEvict = shouldEvictFunction.call();

    return getCacheAction(policyKey, shouldEvict);
  }

  @Override
  public synchronized CacheAction removeEntry(CachePolicyKey policyKey) {
    OffHeapCacheEntry cacheEntry = cacheEntryAccessor.wrap(policyKey.getAddress());

    removeStatsNode(cacheEntry);

    return getCacheAction(policyKey, shouldEvictFunction.call());
  }

  private CacheAction getCacheAction(CachePolicyKey policyKey, Boolean shouldEvict) {
    long tokenToRemove = tailNodeAddress == MemoryConstants.NO_ADDRESS ?
      MemoryConstants.NO_ADDRESS :
      cacheEntryAccessor.wrap(tailNodeAddress).getDataPointer();

    if (shouldEvict && tokenToRemove != MemoryConstants.NO_ADDRESS) {
      cacheEntryAccessor.free(tailNodeAddress);
    }

    return new CacheAction(policyKey, shouldEvict, tokenToRemove);
  }

  private void insertAtHead(OffHeapCacheEntry cacheEntry) {
    cacheEntry.setPrevious(MemoryConstants.NO_ADDRESS);

    if (headNodeAddress == MemoryConstants.NO_ADDRESS) {
      cacheEntry.setNext(MemoryConstants.NO_ADDRESS);
      headNodeAddress = cacheEntry.getAddress();
      tailNodeAddress = cacheEntry.getAddress();
    } else {
      OffHeapCacheEntry headNode = cacheEntryAccessor.wrap(headNodeAddress);

      headNode.setPrevious(cacheEntry.getAddress());
      cacheEntry.setNext(headNodeAddress);
      headNodeAddress = cacheEntry.getAddress();
    }
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
      OffHeapCacheEntryImpl.wrap(previous)
        .setNext(next);
    }

    if (next == MemoryConstants.NO_ADDRESS) {
      tailNodeAddress = previous;
    } else {
      OffHeapCacheEntryImpl.wrap(next)
        .setPrevious(previous);
    }
  }
}
