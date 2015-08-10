package com.facebook.memory.data.structures;

import com.google.common.annotations.VisibleForTesting;
import sun.misc.Unsafe;

import javax.annotation.Nullable;

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

    for (int i = 0; i < numberOfBuckets; i++) {
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
    Bucket bucket = getBucket(wrappedKey);

    if (bucket == null) {
      return null;
    }

    AnnotatedByteArray annotatedByteArray = bucket.get(wrappedKey.getArray());

    if (annotatedByteArray == null) {
      return null;
    } else {
      CachePolicyKey cachePolicyKey = new CachePolicyKey(annotatedByteArray.getAnnotationAddress());

      cachePolicy.updateEntry(cachePolicyKey);
      enforceCachePolicy(cachePolicyKey);

      return annotatedByteArray.getByteArray();
    }
  }

  @Override
  public void put(byte[] key, byte[] value) throws FailedAllocationException {
    put(ByteArray.wrap(key), value);
  }

  @VisibleForTesting
  void put(ByteArray wrappedKey, byte[] value) throws FailedAllocationException {
    Bucket bucket = getBucket(wrappedKey);

    if (bucket == null) {
      bucket = createBucket(wrappedKey);
    }

    AnnotatableMemoryAddress entryAddress = bucket.put(wrappedKey.getArray(), value);
    // the cache policy returns a token for its system by which to refer to this key/value pair
    CacheAction cacheAction = cachePolicy.addEntry(entryAddress);
    // notify the entryAddress of the token used with the cache policy
    entryAddress.storeAnnotationAddress(cacheAction.getCachePolicyKey());

    enforceCachePolicy(cacheAction);
  }

  @Override
  public boolean remove(byte[] key) {
    Bucket bucket = getBucket(ByteArray.wrap(key));

    return bucket != null && bucket.remove(key);
  }

  private void enforceCachePolicy(CachePolicyKey cachePolicyKey) {
    CacheAction cacheAction = cachePolicy.updateEntry(cachePolicyKey);

    enforceCachePolicy(cacheAction);
  }

  private void enforceCachePolicy(CacheAction cacheAction) {

    if (cacheAction.isShouldEvict()) {
      long tokenToRemove = cacheAction.getTokenToEvict();

      if (tokenToRemove != MemoryConstants.NO_ADDRESS) {
        AnnotatedByteArray keyToRemove = keyAccessor.wrap(tokenToRemove);

        remove(keyToRemove.getByteArray());
      }
    }
  }

  @Nullable
  private Bucket getBucket(byte[] key) {
    return getBucket(ByteArray.wrap(key));
  }

  @VisibleForTesting
  @Nullable
  Bucket getBucket(ByteArray wrappedKey) {
    int bucketNumber = wrappedKey.hashCode() % numberOfBuckets;
    long bucketAddress = buckets[bucketNumber];

    return bucketAddress == MemoryConstants.NO_ADDRESS ? null : bucketAccessor.wrap(bucketAddress);
  }

  private Bucket createBucket(ByteArray wrappedKey) throws FailedAllocationException {
    int bucketNumber = wrappedKey.hashCode() % numberOfBuckets;

    return createBucket(bucketNumber);
  }

  private Bucket createBucket(int bucketNumber) throws FailedAllocationException {
    Bucket bucket = bucketAccessor.create();

    UNSAFE.compareAndSwapLong(
      buckets, ARRAY_BASE_OFFSET + bucketNumber * ARRAY_INDEX_SCALE, MemoryConstants.NO_ADDRESS, bucket.getAddress()
    );

    return bucketAccessor.wrap(getBucketAddress(bucketNumber));
  }

  /**
    * @param bucketNumber
   * @return address of this bucket, possibly MemoryConstants.NO_ADDRESS indicating the bucket is not yet allocated
   */
  private long getBucketAddress(int bucketNumber) {
    return UNSAFE.getLong(buckets, ARRAY_BASE_OFFSET + bucketNumber * ARRAY_INDEX_SCALE);
  }

  @Override
  public boolean containsKey(byte[] key) {
    Bucket bucket = getBucket(key);

    return bucket != null && bucket.get(key) != null;
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
