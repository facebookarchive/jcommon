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

import static org.testng.Assert.assertEquals;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.testng.annotations.Test;

public abstract class TestEstimator {
  @Test
  public void testUpdatesBucket() {
    Estimator estimator = getEstimator();

    for (int value = 0; value < estimator.getMaxAllowedBucketValue(); ++value) {
      for (int i = 0; i < estimator.getNumberOfBuckets(); i++) {
        estimator.setIfGreater(i, value);
      }

      for (int bucket : estimator.buckets()) {
        assertEquals(bucket, value);
      }
    }
  }

  @Test
  public void testSetBuckets() {
    Estimator estimator = getEstimator();

    Set<Integer> buckets = new HashSet<Integer>();
    Random random = new Random(0);
    for (int i = 0; i < estimator.getNumberOfBuckets() / 2; i++) {
      int value = random.nextInt(estimator.getNumberOfBuckets());
      buckets.add(value);

      estimator.setIfGreater(i, 1);
    }

    for (int bucket : estimator.buckets()) {
      int expected = 0;
      if (buckets.contains(bucket)) {
        expected = 1;
      }

      assertEquals(bucket, expected);
    }
  }

  protected abstract Estimator getEstimator();
}
