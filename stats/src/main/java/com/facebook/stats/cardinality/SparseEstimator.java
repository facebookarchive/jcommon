/*
 * Copyright (C) 2012 Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.stats.cardinality;

import com.google.common.base.Preconditions;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.Arrays;

/**
 * A low (compact) cardinality estimator
 */
@NotThreadSafe
class SparseEstimator
  implements Estimator {
  private final static int BITS_PER_BUCKET = 4;
  private final static int BUCKET_VALUE_MASK = (1 << BITS_PER_BUCKET) - 1;
  public final static int MAX_BUCKET_VALUE = (1 << BITS_PER_BUCKET);

  private final static int INSTANCE_SIZE = UnsafeUtil.sizeOf(SparseEstimator.class);

  // number of bits used for bucket index
  private final byte indexBits;

  private short bucketCount = 0;

  /*
   This structure keeps a sorted list of bucket entries of size log2(numberOfBuckets) +
    BITS_PER_BUCKET packed into an array of longs.

   Within each long, buckets are stored in little-endian order, aligned to the least-significant bit
    edge. For instance, if a bucket entry is 16 bits long (4 buckets per slot), the layout is:

   slot 0:  [ index 3 | index 2 | index 1 | index 0 ]
   slot 1:  [ index 7 | index 6 | index 5 | index 4 ]
   ....
  */
  private long[] slots;

  SparseEstimator(int numberOfBuckets) {
    this(numberOfBuckets, 1);
  }

  SparseEstimator(int[] buckets) {
    this(buckets.length, countNonZeroBuckets(buckets));

    for (int bucket = 0; bucket < buckets.length; bucket++) {
      int value = buckets[bucket];

      if (value != 0) {
        setEntry(bucketCount, bucket, value);
        ++bucketCount;
      }
    }
  }

  SparseEstimator(int numberOfBuckets, int initialCapacity) {
    Preconditions.checkArgument(
      Numbers.isPowerOf2(numberOfBuckets),
      "numberOfBuckets must be a power of 2"
    );

    this.indexBits = (byte) Integer.numberOfTrailingZeros(numberOfBuckets); // log2(numberOfBuckets)
    slots = new long[(initialCapacity + getBucketsPerSlot()) / getBucketsPerSlot()];
  }

  public boolean setIfGreater(int bucket, int highestBitPosition) {
    Preconditions.checkArgument(
      highestBitPosition < MAX_BUCKET_VALUE,
      "highestBitPosition %s is bigger than allowed by BITS_PER_BUCKET (%s)",
      highestBitPosition,
      BITS_PER_BUCKET
    );

    if (highestBitPosition == 0) {
      return false; // no need to set anything -- 0 is implied if bucket is not present
    }

    int index = findBucket(bucket);

    if (index < 0) {
      insertAt(-(index + 1), bucket, highestBitPosition);
      return true;
    }

    if (getEntry(index).getValue() < highestBitPosition) {
      setEntry(index, bucket, highestBitPosition);
      return true;
    }

    return false;
  }

  public int[] buckets() {
    int[] buckets = new int[getNumberOfBuckets()];

    for (int i = 0; i < bucketCount; ++i) {
      Entry entry = getEntry(i);
      buckets[entry.getBucket()] = entry.getValue();
    }

    return buckets;
  }

  public int getNumberOfBuckets() {
    return 1 << indexBits;
  }

  @Override
  public int getMaxAllowedBucketValue() {
    return MAX_BUCKET_VALUE;
  }

  private Entry getEntry(int index) {
    int totalBitsPerBucket = getTotalBitsPerBucket();
    int bucketMask = (1 << totalBitsPerBucket) - 1;
    int bucketsPerSlot = getBucketsPerSlot();

    int slot = index / bucketsPerSlot;
    int offset = index % bucketsPerSlot;

    int bucketEntry = (int) ((slots[slot] >>> (offset * totalBitsPerBucket)) & bucketMask);

    return new Entry(bucketEntry >> BITS_PER_BUCKET, bucketEntry & BUCKET_VALUE_MASK);
  }

  private int getBucketsPerSlot() {
    return Long.SIZE / getTotalBitsPerBucket();
  }

  private int getTotalBitsPerBucket() {
    return indexBits + BITS_PER_BUCKET;
  }

  private void setEntry(int index, int bucket, int value) {
    int totalBitsPerBucket = getTotalBitsPerBucket();
    long bucketMask = (1L << totalBitsPerBucket) - 1;
    int bucketsPerSlot = getBucketsPerSlot();

    int slot = index / bucketsPerSlot;
    int offset = index % bucketsPerSlot;

    long bucketEntry = (bucket << BITS_PER_BUCKET) | value;

    long bucketClearMask = bucketMask << (offset * totalBitsPerBucket);
    long bucketSetMask = bucketEntry << (offset * totalBitsPerBucket);

    slots[slot] = (slots[slot] & ~bucketClearMask) | bucketSetMask;
  }

  public int estimateSizeInBytes() {
    return estimateSizeInBytes(bucketCount, getNumberOfBuckets());
  }

  public static int estimateSizeInBytes(int nonZeroBuckets, int totalBuckets) {
    Preconditions.checkArgument(
      Numbers.isPowerOf2(totalBuckets),
      "totalBuckets must be a power of 2"
    );

    int bits = Integer.numberOfTrailingZeros(totalBuckets); // log2(totalBuckets)
    int bucketsPerSlot = Long.SIZE / (bits + BITS_PER_BUCKET);

    return (nonZeroBuckets + bucketsPerSlot) / bucketsPerSlot * Long.SIZE / 8 + INSTANCE_SIZE;
  }

  public long estimate() {
    int totalBuckets = getNumberOfBuckets();

    // small cardinality estimate
    int zeroBuckets = totalBuckets - bucketCount;
    return Math.round(totalBuckets * Math.log(totalBuckets * 1.0 / zeroBuckets));
  }

  private void grow() {
    slots = Arrays.copyOf(slots, slots.length + 1);
  }

  private int findBucket(int bucket) {
    int low = 0;
    int high = bucketCount - 1;

    while (low <= high) {
      int middle = (low + high) >>> 1;

      Entry middleBucket = getEntry(middle);

      if (bucket > middleBucket.getBucket()) {
        low = middle + 1;
      } else if (bucket < middleBucket.getBucket()) {
        high = middle - 1;
      } else {
        return middle;
      }
    }

    return -(low + 1); // not found... return insertion point
  }


  private void insertAt(int index, int bucket, int value) {
    int totalBitsPerBucket = getTotalBitsPerBucket();
    int bucketsPerSlot = getBucketsPerSlot();

    ++bucketCount;

    if ((bucketCount + bucketsPerSlot - 1) / bucketsPerSlot > slots.length) {
      grow();
    }

    // the last slot that would have any data after the bucket is inserted
    int lastUsedSlot = (bucketCount - 1) / bucketsPerSlot;

    int insertAtSlot = index / bucketsPerSlot;
    int insertOffset = index % bucketsPerSlot;

    long bucketMask = (1L << totalBitsPerBucket) - 1;

    // shift all buckets one position to the right
    for (int i = lastUsedSlot; i > insertAtSlot; --i) {
      int overflow = (int) ((slots[i - 1] >>> ((bucketsPerSlot - 1) * totalBitsPerBucket)) &
                              bucketMask);
      slots[i] = (slots[i] << totalBitsPerBucket) | overflow;
    }

    long old = slots[insertAtSlot];

    long bottomMask = (1L << (insertOffset * totalBitsPerBucket)) - 1;
    long topMask = 0;
    if (insertOffset < this.getBucketsPerSlot() - 1) {
      // to get around the fact that X << 64 == X, not 0
      topMask = (0xFFFFFFFFFFFFFFFFL << ((insertOffset + 1) * totalBitsPerBucket));
    }
    long bucketSetMask = ((((long) bucket) << BITS_PER_BUCKET) | value) << (insertOffset *
      totalBitsPerBucket);

    slots[insertAtSlot] = ((old << totalBitsPerBucket) & topMask) | bucketSetMask |
      (old & bottomMask);
  }

  private static int countNonZeroBuckets(int[] buckets) {
    int count = 0;
    for (int bucket : buckets) {
      if (bucket > 0) {
        ++count;
      }
    }

    return count;
  }

  private static class Entry {
    private final int bucket;
    private final int value;

    private Entry(int bucket, int value) {
      this.bucket = bucket;
      this.value = value;
    }

    public int getBucket() {
      return bucket;
    }

    public int getValue() {
      return value;
    }
  }
}
