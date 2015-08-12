package com.facebook.memory.data.structures;

import com.google.common.annotations.VisibleForTesting;
import sun.misc.Unsafe;

import javax.annotation.Nullable;

import com.facebook.collections.bytearray.ByteArray;
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
  private final OffHeapByteArrayWrapper keyWrapper;
  private final CachePolicy cachePolicy;

  /**
   *
   * @param numberOfBuckets
   * @param keyWrapper - the CachePolicy will return a token (long) and this maps the token to a ByteArray representing
   *                   the key to be removed
   * @param bucketAccessor - given a token (long), create on-heap Bucket. Also provides a create() method to create a
   *                       new off-heap bucket
   * @param cachePolicy - policy object which indicates when this object should remove items from its cache
   */
  public ByteArrayHashMap(
    int numberOfBuckets, OffHeapByteArrayWrapper keyWrapper, BucketAccessor bucketAccessor, CachePolicy cachePolicy
  ) {
    this.numberOfBuckets = numberOfBuckets;
    this.keyWrapper = keyWrapper;
    this.cachePolicy = cachePolicy;
    buckets = new long[numberOfBuckets];

    for (int i = 0; i < numberOfBuckets; i++) {
      buckets[i] = MemoryConstants.NO_ADDRESS;
    }

    this.bucketAccessor = bucketAccessor;
  }

  @Override
  public ByteArray get(ByteArray key) {
    Bucket bucket = getBucket(key);

    if (bucket == null) {
      return null;
    }

    AnnotatedByteArray annotatedByteArray = bucket.get(key);

    if (annotatedByteArray == null) {
      return null;
    } else {
      CachePolicyKey cachePolicyKey = new CachePolicyKey(annotatedByteArray.getAnnotationAddress());

      CacheAction cacheAction = cachePolicy.updateEntry(cachePolicyKey);

      enforceCachePolicy(cacheAction);

      return annotatedByteArray.getByteArray();
    }
  }

  @Override
  public void put(ByteArray key, ByteArray value) throws FailedAllocationException {
    Bucket bucket = getBucket(key);

    if (bucket == null) {
      bucket = createBucket(key);
    }

    AnnotatableMemoryAddress entryAddress = bucket.put(key, value);
    // the cache policy returns a token for its system by which to refer to this key/value pair
    CacheAction cacheAction = cachePolicy.addEntry(entryAddress);
    // notify the entryAddress of the token used with the cache policy
    entryAddress.storeAnnotationAddress(cacheAction.getCachePolicyKey());

    enforceCachePolicy(cacheAction);
  }

  @Override
  public boolean remove(ByteArray key) {
    Bucket bucket = getBucket(key);

    return bucket != null && bucket.remove(key);
  }

  private void enforceCachePolicy(CacheAction cacheAction) {
    if (cacheAction.isShouldEvict()) {
      long tokenToRemove = cacheAction.getTokenToEvict();

      if (tokenToRemove != MemoryConstants.NO_ADDRESS) {
        OffHeapByteArray offHeapKey = keyWrapper.wrap(tokenToRemove);

        remove(offHeapKey);
      }
    }
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

    long bucketAddress = getBucketAddress(bucketNumber);
    return bucketAccessor.wrap(bucketAddress);
  }

  /**
    * @param bucketNumber
   * @return address of this bucket, possibly MemoryConstants.NO_ADDRESS indicating the bucket is not yet allocated
   */
  private long getBucketAddress(int bucketNumber) {
    return UNSAFE.getLong(buckets, ARRAY_BASE_OFFSET + bucketNumber * ARRAY_INDEX_SCALE);
  }

  @Override
  public boolean containsKey(ByteArray key) {
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
