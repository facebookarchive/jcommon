package com.facebook.memory.data.structures;

import com.google.common.annotations.VisibleForTesting;
import sun.misc.Unsafe;

import com.facebook.collections.ByteArray;
import com.facebook.memory.FailedAllocationException;
import com.facebook.memory.MemoryConstants;
import com.facebook.memory.UnsafeAccessor;

public class ByteArrayHashMap implements ByteArrayMap {
  private static final Unsafe UNSAFE = UnsafeAccessor.get();
  private static final int ARRAY_BASE_OFFSET = UNSAFE.arrayBaseOffset(long[].class);
  private static final int ARRAY_INDEX_SCALE = UNSAFE.arrayIndexScale(long[].class);

  private final long[] buckets;
  private final BucketAccessor bucketAccessor;
  private final int numberOfBuckets;
  private final ByteArrayAccessor keyAccessor;
  private final CachePolicy cachePolicy;

  public ByteArrayHashMap(
    int numberOfBuckets, ByteArrayAccessor keyAccessor, BucketAccessor bucketAccessor, CachePolicy cachePolicy
  ) {
    this.numberOfBuckets = numberOfBuckets;
    this.keyAccessor = keyAccessor;
    this.cachePolicy = cachePolicy;
    buckets = new long[numberOfBuckets];

    for (int i = 0 ; i < numberOfBuckets; i++) {
      buckets[i] = MemoryConstants.NO_ADDRESS;
    }

    this.bucketAccessor = bucketAccessor;
  }

  @Override
  public byte[] get(byte[] key) {
    return get(ByteArray.wrap(key));
  }

  @VisibleForTesting
  byte[] get(ByteArray wrappedKey) {
    AnnotatedByteArray annotatedByteArray = getBucket(wrappedKey).get(wrappedKey.getArray());

    if (annotatedByteArray == null) {
      return null;
    } else {
      CachePolicyKey policyKey = new CachePolicyKey(annotatedByteArray.getAnnotationAddress());

      cachePolicy.updateEntry(policyKey);
      enforceCachePolicy();

      return annotatedByteArray.getByteArray();
    }
  }

  @Override
  public void put(byte[] key, byte[] value) throws FailedAllocationException {
    put(ByteArray.wrap(key), value);
  }

  @VisibleForTesting
  void put(ByteArray wrappedKey, byte[] value) throws FailedAllocationException {
    AnnotatableMemoryAddress entryAddress = getBucket(wrappedKey).put(wrappedKey.getArray(), value);

    CachePolicyKey cachePolicyKey = cachePolicy.addEntry(entryAddress);

    entryAddress.storeAnnotationAddress(cachePolicyKey);

    enforceCachePolicy();
  }

  @Override
  public boolean remove(byte[] key) {
    return getBucket(ByteArray.wrap(key)).remove(key);
  }

  private void enforceCachePolicy() {
    if (cachePolicy.shouldEvict()) {
      long tokenToRemove = cachePolicy.getTokenToRemove();

      if (tokenToRemove != MemoryConstants.NO_ADDRESS) {
        AnnotatedByteArray keyToRemove = keyAccessor.wrap(tokenToRemove);

        remove(keyToRemove.getByteArray());
      }
    }
  }

  private Bucket getBucket(byte[] key) {
    return getBucket(ByteArray.wrap(key));
  }

  @VisibleForTesting
  Bucket getBucket(ByteArray wrappedKey) {
    int bucketNumber = wrappedKey.hashCode() % numberOfBuckets;
    long bucketAddress = buckets[bucketNumber];

    if (bucketAddress == MemoryConstants.NO_ADDRESS) {
      Bucket bucket = bucketAccessor.create();

      UNSAFE.compareAndSwapLong(
        buckets, ARRAY_BASE_OFFSET + bucketNumber * ARRAY_INDEX_SCALE, MemoryConstants.NO_ADDRESS, bucket.getAddress()
      );
//      buckets[bucketNumber] = bucket.getAddress();

      return bucket;
    } else {
      return bucketAccessor.wrap(bucketAddress);
    }
  }

  @Override
  public boolean containsKey(byte[] key) {
    Bucket bucket = getBucket(key);

    return bucket.get(key) != null;
  }

  @Override
  public int getSize() {
    int totalSize = 0;

    for (long address : buckets) {
      totalSize += bucketAccessor.wrap(address).size();
    }

    return totalSize;
  }
}
