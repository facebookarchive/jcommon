package com.facebook.memory.data.structures;

import com.google.common.base.Preconditions;

import java.util.concurrent.atomic.AtomicLong;

import com.facebook.collections.bytearray.ByteArray;

public class MapBucket implements Bucket {
  private static final AtomicLong NEXT_ADDRESS = new AtomicLong(1);

  private final int capacity;
  private final long address;
  private final MapBucketAccessor bucketAccessor;

  public MapBucket(int capacity, long address, MapBucketAccessor bucketAccessor) {
    this.capacity = capacity;
    this.address = address;
    this.bucketAccessor = bucketAccessor;
  }

  @Override
  public synchronized AnnotatedByteArray get(ByteArray key) {
    Long dataAddress = bucketAccessor.keyToAddress.get(key);
    Long cacheKey = bucketAccessor.keyToCacheKey.get(key);
    ByteArray byteArray = bucketAccessor.data.get(key);

    return byteArray == null ? null : new AnnotatedByteArray(dataAddress, cacheKey, byteArray);
  }

  @Override
  public synchronized AnnotatableMemoryAddress put(ByteArray key, ByteArray value) {
    bucketAccessor.data.put(key, value);

    Long address = bucketAccessor.keyToAddress.get(key);

    if (address == null) {
      address = NEXT_ADDRESS.getAndIncrement();
      bucketAccessor.keyToAddress.put(key, address);
      Preconditions.checkState(bucketAccessor.addressToKey.putIfAbsent(address, key) == null);
    }

    return new AnnotatableMemoryAddress(address) {
      @Override
      public void storeAnnotationAddress(OffHeap offHeap) {
        bucketAccessor.keyToCacheKey.put(key, offHeap.getAddress());
      }
    };
  }

  @Override
  public synchronized boolean remove(ByteArray key) {
    bucketAccessor.keyToCacheKey.remove(key);
    bucketAccessor.addressToKey.remove(bucketAccessor.keyToAddress.remove(key));

    return bucketAccessor.data.remove(key) != null;
  }

  @Override
  public long size() {
    return bucketAccessor.data.size();
  }

  @Override
  public long getAddress() {
    return address;
  }
}
