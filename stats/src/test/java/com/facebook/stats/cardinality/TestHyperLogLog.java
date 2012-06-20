package com.facebook.stats.cardinality;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.testng.Assert.assertTrue;

public class TestHyperLogLog {
  @Test(groups = "slow")
  public void testError()
    throws Exception {
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

    Set<Long> result = new HashSet<Long>();
    while (result.size() < count) {
      result.add(random.nextLong());
    }

    return result;
  }
}
