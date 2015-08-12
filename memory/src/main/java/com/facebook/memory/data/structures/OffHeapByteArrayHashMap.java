package com.facebook.memory.data.structures;

import com.google.common.annotations.VisibleForTesting;

import com.facebook.memory.FailedAllocationException;
import com.facebook.memory.data.types.definitions.SizedOffHeapStructure;

/**
 * light wrapper over OffHeapHashMap that handles conversion of the value to an OffHeapByteArray
 */
public class OffHeapByteArrayHashMap implements OffHeapMap<OffHeapByteArray, OffHeapByteArray> {
  private final OffHeapHashMap data;

  public OffHeapByteArrayHashMap(
    int numberOfBuckets, LinkedListKeyWrapper keyWrapper, BucketAccessor bucketAccessor, CachePolicy cachePolicy
  ) {
    data = new OffHeapHashMap(numberOfBuckets, keyWrapper, bucketAccessor, cachePolicy);
  }

  @Override
  public OffHeapByteArray get(OffHeapByteArray key) {
    SizedOffHeapStructure offHeapStructure = data.get(key);
    return offHeapStructure == null ? null : OffHeapByteArrayImpl.wrap(offHeapStructure.getAddress());
  }

  @Override
  public void put(OffHeapByteArray key, OffHeapByteArray value) throws FailedAllocationException {
    data.put(key, value);
  }

  @Override
  public boolean remove(OffHeapByteArray key) {
    return data.remove(key);
  }

  @Override
  public boolean containsKey(OffHeapByteArray key) {
    return data.containsKey(key);
  }

  @Override
  public int getSize() {
    return data.getSize();
  }

  @VisibleForTesting
  Bucket getBucket(SizedOffHeapStructure key) {
    return data.getBucket(key);
  }
}
