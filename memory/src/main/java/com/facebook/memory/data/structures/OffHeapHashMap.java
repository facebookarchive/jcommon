package com.facebook.memory.data.structures;

import com.google.common.annotations.VisibleForTesting;
import sun.misc.Unsafe;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicLong;

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
  private final SizedOffHeapWrapper keyWrapper;
  private final SizedOffHeapWrapper valueWrapper;
  private final CachePolicy cachePolicy;
  private final EvictionFunction evictionFunction;
  private final EvictionCallback evictionCallback;
  private final AtomicLong size= new AtomicLong(0L);

  /**
   * @param numberOfBuckets
   * @param keyWrapper      - This should know how to take the address of a *NODE* in a bucket and return a
   *                        SizedOffHeapStructure for the *KEY ONLY*
   * @param valueWrapper      - This should know how to take the address of a *NODE* in a bucket and return a
   *                        SizedOffHeapStructure for the *VALUE ONLY*
   * @param bucketAccessor  - given a token (long), create on-heap Bucket. Also provides a create() method to create a
   *                        new off-heap bucket
   * @param cachePolicy     - policy object which indicates when this object should remove items from its cache
   * @param evictionCallback - notified when a key or value is removed from the map. This gives callers an opportunity
   *                         to free memory
   */
  public OffHeapHashMap(
    int numberOfBuckets,
    SizedOffHeapWrapper keyWrapper,
    SizedOffHeapWrapper valueWrapper,
    BucketAccessor bucketAccessor,
    CachePolicy cachePolicy,
    EvictionFunction evictionFunction,
    EvictionCallback evictionCallback
  ) {
    this.keyWrapper = keyWrapper;
    this.valueWrapper = valueWrapper;
    this.cachePolicy = cachePolicy;
    this.evictionFunction = evictionFunction;
    this.evictionCallback = evictionCallback;
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
      boolean shouldEvict = evictionFunction.shouldEvict(this);
      CacheAction cacheAction = cachePolicy.updateEntry(cachePolicyKey, shouldEvict);

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

    if (bucketPutResult.isExisting()) { // they key existed
      AnnotatedOffHeapValue existingEntry = bucketPutResult.getExistingEntry();
      SizedOffHeapStructure existingValue = existingEntry.getValue();
      CachePolicyKey cachePolicyKey = new CachePolicyKey(existingEntry.getAnnotationAddress());
      boolean shouldEvict = evictionFunction.shouldEvictOnUpdate(existingValue, value, this);

      cacheAction = cachePolicy.updateEntry(cachePolicyKey, shouldEvict);
      // adjust size by net increase/decrease
      int delta = value.getSize() - existingValue.getSize();

      size.addAndGet(delta);
    } else { // new key/value pair in map
      AnnotatableMemoryAddress entryAddress = bucketPutResult.getNewEntry();
      boolean shouldEvict = evictionFunction.shouldEvictOnNewEntry(key, value, this);
      // the cache policy returns a token for its system by which to refer to this key/value pair
      cacheAction = cachePolicy.addEntry(entryAddress, shouldEvict);
      // notify the entryAddress of the token used with the cache policy
      entryAddress.storeAnnotationAddress(cacheAction.getCachePolicyKey());

      int delta = key.getSize() + value.getSize();

      size.addAndGet(delta);
    }

    enforceCachePolicy(cacheAction);
  }

  @Override
  public boolean remove(SizedOffHeapStructure key) {
    return internalRemove(
      key, value -> {
        CachePolicyKey cachePolicyKey = new CachePolicyKey(value.getAnnotationAddress());
        boolean shouldEvict = evictionFunction.shouldEvict(this);
        CacheAction cacheAction = cachePolicy.removeEntry(cachePolicyKey, shouldEvict);

        enforceCachePolicy(cacheAction);
      }
    );
  }

  @VisibleForTesting
  @Nullable
  Bucket getBucket(SizedOffHeapStructure key) {
    int bucketNumber = key.hashCode() % buckets.length;
    long bucketAddress = buckets[bucketNumber];

    return bucketAddress == MemoryConstants.NO_ADDRESS ? null : bucketAccessor.wrap(bucketAddress);
  }

  /**
   * removes an item without checking cache policy. Updates the tracked size of the map
   *
   * @param key
   * @param evictionFunction
   * @return
   */
  private boolean internalRemove(SizedOffHeapStructure key, CacheEvictionFunction evictionFunction) {
    Bucket bucket = getBucket(key);

    if (bucket != null) {
      BucketEntry bucketEntry = bucket.get(key);

      if (bucketEntry != null) {
        AnnotatedOffHeapValue annotatedValue = bucketEntry.getAnnotatedValue();

        evictionFunction.perform(annotatedValue);
        bucketEntry.remove();

        SizedOffHeapStructure offHeapStructure = annotatedValue.getValue();
        int removedSize = key.getSize() + offHeapStructure.getSize();

        size.addAndGet(-removedSize);

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
        // the keyWrapper knows how to return the structure for just the key
        SizedOffHeapStructure offHeapKey = keyWrapper.wrap(tokenToRemove);
        SizedOffHeapStructure offHeapValue = valueWrapper.wrap(tokenToRemove);

        internalRemove(offHeapKey, value -> {});
        evictionCallback.keyEvicted(offHeapKey);
        evictionCallback.valueEvicted(offHeapValue);
      }
    }
  }

  private Bucket createBucket(SizedOffHeapStructure key) throws FailedAllocationException {
    int bucketNumber = key.hashCode() % buckets.length;

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
  public int getLength() {
    int length = 0;

    for (long address : buckets) {
      if (address != MemoryConstants.NO_ADDRESS) {
        length += bucketAccessor.wrap(address).length();
      }
    }

    return length;
  }

  @Override
  public long getSize() {
    return size.get();
  }

  private interface CacheEvictionFunction {
    void perform(AnnotatedOffHeapValue value);
  }
}
