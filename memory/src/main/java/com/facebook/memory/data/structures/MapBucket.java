package com.facebook.memory.data.structures;

import com.google.common.base.Preconditions;

import java.util.concurrent.atomic.AtomicLong;

import com.facebook.memory.data.BucketPutResult;
import com.facebook.memory.data.types.definitions.SizedOffHeapStructure;

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
  public synchronized BucketEntry get(SizedOffHeapStructure key) {
    Long dataAddress = bucketAccessor.keyToAddress.get(key);
    Long cacheKey = bucketAccessor.keyToCacheKey.get(key);
    SizedOffHeapStructure value = bucketAccessor.data.get(key);

    return value == null ? null :
      new AbstractBucketEntry(
        key,
        new AnnotatedOffHeapValue(value, cacheKey),
        dataAddress
      ) {
        @Override
        public void remove() {
          MapBucket.this.remove(key);
        }
      };
  }

  @Override
  public synchronized BucketPutResult put(SizedOffHeapStructure key, SizedOffHeapStructure value) {
    SizedOffHeapStructure put = bucketAccessor.data.put(key, value);

    Long address = bucketAccessor.keyToAddress.get(key);

    if (address == null) {
      address = NEXT_ADDRESS.getAndIncrement();
      bucketAccessor.keyToAddress.put(key, address);
      Preconditions.checkState(bucketAccessor.addressToKey.putIfAbsent(address, key) == null);
    }

    AnnotatableMemoryAddress annotatableMemoryAddress = new AnnotatableMemoryAddress(address) {
      @Override
      public void storeAnnotationAddress(OffHeap offHeap) {
        bucketAccessor.keyToCacheKey.put(key, offHeap.getAddress());
      }
    };

    BucketEntry bucketEntry = get(key);

    return put == null ?
      BucketPutResult.createNewEntry(annotatableMemoryAddress) :
      BucketPutResult.createExistingEntry(bucketEntry.getAnnotatedValue());

  }

  @Override
  public synchronized boolean remove(SizedOffHeapStructure key) {
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
