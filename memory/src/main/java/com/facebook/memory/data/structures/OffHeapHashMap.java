package com.facebook.memory.data.structures;

import com.google.common.annotations.VisibleForTesting;
import sun.misc.Unsafe;

import javax.annotation.Nullable;

import com.facebook.memory.FailedAllocationException;
import com.facebook.memory.MemoryConstants;
import com.facebook.memory.UnsafeAccessor;
import com.facebook.memory.data.BucketPutResult;
import com.facebook.memory.data.types.definitions.SizedOffHeapStructure;

public class OffHeapHashMap implements OffHeapMap<SizedOffHeapStructure, SizedOffHeapStructure> {
  private static final Unsafe UNSAFE = UnsafeAccessor.get();
  private static final int ARRAY_BASE_OFFSET = UNSAFE.arrayBaseOffset(long[].class);
  private static final int ARRAY_INDEX_SCALE = UNSAFE.arrayIndexScale(long[].class);

  private final long[] buckets;
  private final BucketAccessor bucketAccessor;
  private final int numberOfBuckets;
  private final SizedOffHeapWrapper keyWrapper;
  private final CachePolicy cachePolicy;

  /**
   * @param numberOfBuckets
   * @param keyWrapper      - This should know how to take the address of a *NODE* in a bucket and return a
   *                        SizedOffHeapStructure for the *KEY ONLY*
   * @param bucketAccessor  - given a token (long), create on-heap Bucket. Also provides a create() method to create a
   *                        new off-heap bucket
   * @param cachePolicy     - policy object which indicates when this object should remove items from its cache
   */
  public OffHeapHashMap(
    int numberOfBuckets, LinkedListKeyWrapper keyWrapper, BucketAccessor bucketAccessor, CachePolicy cachePolicy
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
  public SizedOffHeapStructure get(SizedOffHeapStructure key) {
    Bucket bucket = getBucket(key);

    if (bucket == null) {
      return null;
    }

    BucketEntry bucketEntry = bucket.get(key);

    if (bucketEntry == null) {
      return null;
    } else {
      AnnotatedOffHeapValue annotatedValue = bucketEntry.getAnnotatedValue();
      CachePolicyKey cachePolicyKey = new CachePolicyKey(annotatedValue.getAnnotationAddress());

      CacheAction cacheAction = cachePolicy.updateEntry(cachePolicyKey);

      enforceCachePolicy(cacheAction);

      return annotatedValue.getValue();
    }
  }

  @Override
  public void put(SizedOffHeapStructure key, SizedOffHeapStructure value) throws FailedAllocationException {
    Bucket bucket = getBucket(key);

    if (bucket == null) {
      bucket = createBucket(key);
    }

    BucketPutResult bucketPutResult = bucket.put(key, value);
    CacheAction cacheAction;

    if (bucketPutResult.isExisting()) {
      AnnotatedOffHeapValue existingEntry = bucketPutResult.getExistingEntry();
      CachePolicyKey cachePolicyKey = new CachePolicyKey(existingEntry.getAnnotationAddress());

      cacheAction = cachePolicy.updateEntry(cachePolicyKey);
    } else {
      AnnotatableMemoryAddress entryAddress = bucketPutResult.getNewEntry();
      // the cache policy returns a token for its system by which to refer to this key/value pair
      cacheAction = cachePolicy.addEntry(entryAddress);
      // notify the entryAddress of the token used with the cache policy
      entryAddress.storeAnnotationAddress(cacheAction.getCachePolicyKey());
    }

    enforceCachePolicy(cacheAction);
  }

  @Override
  public boolean remove(SizedOffHeapStructure key) {
    return internalRemove(
      key, value -> {
        CachePolicyKey cachePolicyKey = new CachePolicyKey(value.getAnnotationAddress());
        CacheAction cacheAction = cachePolicy.removeEntry(cachePolicyKey);

        enforceCachePolicy(cacheAction);
      }
    );
  }

  private boolean internalRemove(SizedOffHeapStructure key, CacheEvictionFunction evictionFunction) {
    Bucket bucket = getBucket(key);

    if (bucket != null) {
      BucketEntry bucketEntry = bucket.get(key);

      if (bucketEntry != null) {
        evictionFunction.perform(bucketEntry.getAnnotatedValue());
        bucketEntry.remove();

        return true;
      }
    }

    return false;
  }

  private void enforceCachePolicy(CacheAction cacheAction) {
    if (cacheAction.isShouldEvict()) {
      // tokenToRemove will be the address of a LinkedListBucketNode to remove
      long tokenToRemove = cacheAction.getTokenToEvict();

      if (tokenToRemove != MemoryConstants.NO_ADDRESS) {
        SizedOffHeapStructure offHeapKey = keyWrapper.wrap(tokenToRemove);

        internalRemove(offHeapKey, value -> {});
      }
    }
  }

  @VisibleForTesting
  @Nullable
  Bucket getBucket(SizedOffHeapStructure key) {
    int bucketNumber = key.hashCode() % numberOfBuckets;
    long bucketAddress = buckets[bucketNumber];

    return bucketAddress == MemoryConstants.NO_ADDRESS ? null : bucketAccessor.wrap(bucketAddress);
  }

  private Bucket createBucket(SizedOffHeapStructure key) throws FailedAllocationException {
    int bucketNumber = key.hashCode() % numberOfBuckets;

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
  public boolean containsKey(SizedOffHeapStructure key) {
    Bucket bucket = getBucket(key);

    return bucket != null && bucket.get(key) != null;
  }

  @Override
  public int getSize() {
    int totalSize = 0;

    for (long address : buckets) {
      if (address != MemoryConstants.NO_ADDRESS) {
        totalSize += bucketAccessor.wrap(address).size();
      }
    }

    return totalSize;
  }

  private static interface CacheEvictionFunction {
    void perform(AnnotatedOffHeapValue value);
  }
}
