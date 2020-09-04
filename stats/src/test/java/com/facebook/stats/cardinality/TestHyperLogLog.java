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

import static org.testng.Assert.assertTrue;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.testng.annotations.Test;

public class TestHyperLogLog {
  @Test(groups = "slow")
  public void testError() throws Exception {
    DescriptiveStatistics stats = new DescriptiveStatistics();
    int buckets = 2048;
    for (int i = 0; i < 10000; ++i) {
      HyperLogLog estimator = new HyperLogLog(buckets);
      Set<Long> randomSet = makeRandomSet(5 * buckets);
      for (Long value : randomSet) {
        estimator.add(value);
      }

      double error = (estimator.estimate() - randomSet.size()) * 1.0 / randomSet.size();
      stats.addValue(error);
    }

    assertTrue(stats.getMean() < 1e-2);
    assertTrue(stats.getStandardDeviation() < 1.04 / Math.sqrt(buckets));
  }

  private Set<Long> makeRandomSet(int count) {
    Random random = new Random();

    Set<Long> result = new HashSet<>();
    while (result.size() < count) {
      result.add(random.nextLong());
    }

    return result;
  }
}
