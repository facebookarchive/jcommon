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

import static com.facebook.stats.cardinality.HyperLogLogUtil.computeHash;

/**
 * An implementation of the HyperLogLog algorithm:
 * <p/>
 * http://algo.inria.fr/flajolet/Publications/FlFuGaMe07.pdf
 */
@NotThreadSafe
public class HyperLogLog {
  private final byte[] buckets;

  // The current sum of 1 / (1L << buckets[i]). Updated as new items are added and used for
  // estimation
  private double currentSum;
  private int nonZeroBuckets = 0;

  public HyperLogLog(int numberOfBuckets) {
    Preconditions.checkArgument(
      Numbers.isPowerOf2(numberOfBuckets),
      "numberOfBuckets must be a power of 2"
    );
    Preconditions.checkArgument(numberOfBuckets > 0, "numberOfBuckets must be > 0");

    buckets = new byte[numberOfBuckets];
    currentSum = buckets.length;
  }

  public HyperLogLog(int[] buckets) {
    this(buckets.length);

    currentSum = 0;
    for (int i = 0; i < buckets.length; i++) {
      int value = buckets[i];
      Preconditions.checkArgument(
        value >= 0 && value <= Byte.MAX_VALUE,
        "values must be > 0 and <= %s, found %s",
        Byte.MAX_VALUE,
        value
      );
      this.buckets[i] = (byte) value;
      currentSum += 1.0 / (1 << value);
      if (value != 0) {
        nonZeroBuckets++;
      }
    }
  }

  public void add(long value) {
    BucketAndHash bucketAndHash = BucketAndHash.fromHash(computeHash(value), buckets.length);
    int bucket = bucketAndHash.getBucket();

    int lowestBitPosition = Long.numberOfTrailingZeros(bucketAndHash.getHash()) + 1;

    int previous = buckets[bucket];

    if (previous == 0) {
      nonZeroBuckets++;
    }

    if (lowestBitPosition > previous) {
      currentSum -= 1.0 / (1L << previous);
      currentSum += 1.0 / (1L << lowestBitPosition);

      buckets[bucket] = (byte) lowestBitPosition;
    }
  }

  public long estimate() {
    double alpha = HyperLogLogUtil.computeAlpha(buckets.length);
    double result = alpha * buckets.length * buckets.length / currentSum;

    if (result <= 2.5 * buckets.length) {
      // adjust for small cardinalities
      int zeroBuckets = buckets.length - nonZeroBuckets;
      if (zeroBuckets > 0) {
        result = buckets.length * Math.log(buckets.length * 1.0 / zeroBuckets);
      }
    }

    return Math.round(result);
  }

  public int[] buckets() {
    int[] result = new int[buckets.length];

    for (int i = 0; i < buckets.length; i++) {
      result[i] = buckets[i];
    }

    return result;
  }
}
