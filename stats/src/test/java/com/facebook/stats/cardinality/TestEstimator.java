package com.facebook.stats.cardinality;

import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.testng.Assert.assertEquals;

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
