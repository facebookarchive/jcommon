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

import static com.google.common.base.Preconditions.checkArgument;

public class BucketAndHash {
  private final int bucket;
  private final long hash;

  BucketAndHash(int bucket, long hash) {
    this.bucket = bucket;
    this.hash = hash;
  }

  /**
   * Extracts the bucket and truncated hash from the given 64-bit hash.
   *
   * <p>Only the 64-log2(numberOfBuckets) least significant bits of the resulting hash are usable
   */
  public static BucketAndHash fromHash(long hash, int numberOfBuckets) {
    checkArgument(Numbers.isPowerOf2(numberOfBuckets), "numberOfBuckets must be a power of 2");

    // bucket comes from the bottommost log2(numberOfBuckets) bits
    int bucketMask = numberOfBuckets - 1;
    int bucket = (int) (hash & bucketMask);

    // hyperloglog will count number of trailing zeros, so fill in with ones at the top to avoid
    // the msb bits affecting the count (very unlikely, but...)

    // first, set the top most bit
    hash |= 1L << (Long.SIZE - 1);

    // then, shift with sign propagation to fill with ones
    int bits = Integer.numberOfTrailingZeros(numberOfBuckets); // log2(numberOfBuckets)
    hash >>= bits;

    return new BucketAndHash(bucket, hash);
  }

  public int getBucket() {
    return bucket;
  }

  public long getHash() {
    return hash;
  }
}
