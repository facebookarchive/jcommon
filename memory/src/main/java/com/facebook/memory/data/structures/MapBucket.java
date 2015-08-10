package com.facebook.memory.data.structures;

import com.google.common.base.Preconditions;

import java.util.concurrent.atomic.AtomicLong;

import com.facebook.collections.ByteArray;

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
  public synchronized AnnotatedByteArray get(byte[] key) {
    ByteArray wrappedKey = ByteArray.wrap(key);
    Long dataAddress = bucketAccessor.keyToAddress.get(wrappedKey);
    Long cacheKey = bucketAccessor.keyToCacheKey.get(wrappedKey);
    ByteArray byteArray = bucketAccessor.data.get(wrappedKey);

    return byteArray == null ? null : new AnnotatedByteArray(dataAddress, cacheKey, byteArray.getArray());
  }

  @Override
  public synchronized AnnotatableMemoryAddress put(byte[] key, byte[] value) {
    final ByteArray wrappedKey = ByteArray.wrap(key);

    bucketAccessor.data.put(wrappedKey, ByteArray.wrap(value));

    Long address = bucketAccessor.keyToAddress.get(wrappedKey);

    if (address == null) {
      address = NEXT_ADDRESS.getAndIncrement();
      bucketAccessor.keyToAddress.put(wrappedKey, address);
      Preconditions.checkState(bucketAccessor.addressToKey.putIfAbsent(address, wrappedKey) == null);
    }

    return new AnnotatableMemoryAddress(address) {
      @Override
      public void storeAnnotationAddress(OffHeap offHeap) {
        bucketAccessor.keyToCacheKey.put(wrappedKey, offHeap.getAddress());
      }
    };
  }

  @Override
  public synchronized boolean remove(byte[] key) {
    ByteArray wrappedKey = ByteArray.wrap(key);

    bucketAccessor.keyToCacheKey.remove(wrappedKey);
    bucketAccessor.addressToKey.remove(bucketAccessor.keyToAddress.remove(wrappedKey));

    return bucketAccessor.data.remove(wrappedKey) != null;
  }

  @Override
  public long size() {
    return bucketAccessor.data.size();
  }

  @Override
  public long capacity() {
    return capacity;
  }

  @Override
  public long getAddress() {
    return address;
  }
}
