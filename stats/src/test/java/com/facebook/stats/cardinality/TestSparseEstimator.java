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

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;

import static org.testng.Assert.assertEquals;

public class TestSparseEstimator
  extends TestEstimator {
  @Override
  protected Estimator getEstimator() {
    return new SparseEstimator(1024);
  }

  @Test
  public void testRoundtrip() {
    Estimator estimator = getEstimator();

    for (int i = 0; i < estimator.getNumberOfBuckets(); i++) {
      estimator.setIfGreater(i, i % 16);
    }

    SparseEstimator other = new SparseEstimator(estimator.buckets());
    assertEquals(estimator.buckets(), other.buckets());
  }

  @Test
  public void testBucketsConstructor() {
    int[] buckets = new int[1024];

    for (int bucket = 0; bucket < buckets.length; ++bucket) {
      for (int maxBit = 0; maxBit < 16; ++maxBit) {
        buckets[bucket] = maxBit;

        SparseEstimator actual = new SparseEstimator(buckets);
        SparseEstimator expected = new SparseEstimator(buckets.length);

        for (int x = 0; x < buckets.length; ++x) {
          expected.setIfGreater(x, buckets[x]);
        }

        // Assert.assertEquals() is much slower -- using it doubles the test time!
        if (!Arrays.equals(actual.buckets(), expected.buckets())) {
          Assert.fail(bucket + " " + maxBit);
        }
      }
    }
  }
}
