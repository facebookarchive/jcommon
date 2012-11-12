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

/**
 * A hyperloglog-based cardinality estimator that uses exactly 4 bits per bucket, regardless of the
 * cardinality being estimated.
 * <p/>
 * It is based on the observation that for any given cardinality, the majority of all values fall in
 * a range that is at most 4-bit wide. Moreover, the window only moves to the "right" because the
 * values in a bucket never decrease.
 * <p/>
 * Whenever a value is seen that falls outside of the current window it is truncated to the window
 * upper bound. This introduces a minor error in the estimation that is smaller than 0.01% based on
 * experiments.
 */
@NotThreadSafe
class DenseEstimator
  implements Estimator {
  private static final int BITS_PER_BUCKET = 4;
  private static final int BUCKET_MAX_VALUE = (1 << BITS_PER_BUCKET) - 1;
  private static final int BUCKETS_PER_SLOT = Long.SIZE / BITS_PER_BUCKET;
  private static final long BUCKET_MASK = (1L << BITS_PER_BUCKET) - 1;

  private static final int INSTANCE_SIZE = UnsafeUtil.sizeOf(DenseEstimator.class);

  private final int numberOfBuckets;
  private final long[] slots;

  private double currentSum; // the current sum(1 / (1 << (bucket[i] + baseline)))
  private byte baseline; // the lower bound of the current window
  private short baselineCount; // the number of buckets who's value is at the lower bound

  public DenseEstimator(int numberOfBuckets) {
    Preconditions.checkArgument(
      Numbers.isPowerOf2(numberOfBuckets),
      "numberOfBuckets must be a power of 2"
    );

    this.numberOfBuckets = numberOfBuckets;
    this.baseline = 0;
    this.baselineCount = (short) numberOfBuckets;
    this.currentSum = numberOfBuckets;

    int slotCount = (numberOfBuckets + BUCKETS_PER_SLOT - 1) / BUCKETS_PER_SLOT;
    slots = new long[slotCount];
  }

  public DenseEstimator(int[] bucketValues) {
    this(bucketValues.length);

    // first, compute the baseline and count of baseline values
    baseline = Byte.MAX_VALUE;
    baselineCount = 0;
    for (int value : bucketValues) {
      Preconditions.checkArgument(
        value >= 0 && value <= Byte.MAX_VALUE,
        "values must be >= 0 and <= %s, found %s",
        Byte.MAX_VALUE,
        value
      );
      if (value < baseline) {
        baselineCount = 1;
        baseline = (byte) value;
      } else if (value == baseline) {
        ++baselineCount;
      }
    }

    currentSum = 0;

    // then set all values (rescaled)
    int bucket = 0;
    for (int value : bucketValues) {
      set(bucket, value - baseline);
      currentSum += 1.0 / (1L << value);
      ++bucket;
    }
  }

  public int getNumberOfBuckets() {
    return numberOfBuckets;
  }

  @Override
  public int getMaxAllowedBucketValue() {
    return Byte.MAX_VALUE;
  }

  @Override
  public boolean setIfGreater(int bucket, int highestBitPosition) {
    int relativeHighestBitPosition = highestBitPosition - baseline;
    if (relativeHighestBitPosition > BUCKET_MAX_VALUE) {
      // we can't fit this in BITS_PER_BUCKET, so truncate (it shouldn't affect results
      // significantly due to the low probability of this happening)
      relativeHighestBitPosition = BUCKET_MAX_VALUE;
    }

    int oldValue = get(bucket);

    if (relativeHighestBitPosition <= oldValue) {
      return false;
    }

    set(bucket, relativeHighestBitPosition);

    currentSum -= 1.0 / (1L << (oldValue + baseline));
    currentSum += 1.0 / (1L << (relativeHighestBitPosition + baseline));

    if (oldValue == 0) {
      --baselineCount;
      rescaleAndRecomputeBaseCountIfNeeded();
    }

    return true;
  }

  private void set(int bucket, int value) {
    int slot = bucket / BUCKETS_PER_SLOT;
    int offset = bucket % BUCKETS_PER_SLOT;

    // clear the old value
    long bucketClearMask = BUCKET_MASK << (offset * BITS_PER_BUCKET);
    slots[slot] &= ~bucketClearMask;

    // set the new value
    long bucketSetMask = ((long) value) << (offset * BITS_PER_BUCKET);
    slots[slot] |= bucketSetMask;
  }

  /**
   * gets the value in the specified bucket relative to the current base
   */
  private int get(int bucket) {
    int slot = bucket / BUCKETS_PER_SLOT;
    int offset = bucket % BUCKETS_PER_SLOT;

    return (int) ((slots[slot] >> (offset * BITS_PER_BUCKET)) & BUCKET_MASK);
  }

  private void rescaleAndRecomputeBaseCountIfNeeded() {
    while (baselineCount == 0) {
      // no more values at the lower bound, so shift the window to the right

      ++baseline;
      baselineCount = 0;

      // and re-scale all current buckets
      for (int i = 0; i < numberOfBuckets; ++i) {
        int value = get(i);
        --value;

        set(i, value);

        if (value == 0) {
          // re-calculate the number of buckets who's value is at the lower bound
          ++baselineCount;
        }
      }
    }
  }

  @Override
  public long estimate() {
    double alpha = HyperLogLogUtil.computeAlpha(numberOfBuckets);
    double result = alpha * numberOfBuckets * numberOfBuckets / currentSum;

    if (result <= 2.5 * numberOfBuckets) {
      // adjust for small cardinalities
      if (baseline == 0 && baselineCount > 0) {
        // baselineCount is the number of buckets with value 0
        result = numberOfBuckets * Math.log(numberOfBuckets * 1.0 / baselineCount);
      }
    }

    return Math.round(result);
  }

  @Override
  public int estimateSizeInBytes() {
    return estimateSizeInBytes(numberOfBuckets);
  }

  public static int estimateSizeInBytes(int numberOfBuckets) {
    return (numberOfBuckets + BUCKETS_PER_SLOT - 1) / BUCKETS_PER_SLOT * Long.SIZE / 8
      + INSTANCE_SIZE;
  }

  public int[] buckets() {
    int[] result = new int[numberOfBuckets];

    for (int i = 0; i < numberOfBuckets; ++i) {
      result[i] = get(i) + baseline;
    }

    return result;
  }
}
