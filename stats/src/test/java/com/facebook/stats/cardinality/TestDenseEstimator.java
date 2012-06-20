package com.facebook.stats.cardinality;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class TestDenseEstimator
  extends TestEstimator {
  @Override
  protected Estimator getEstimator() {
    return new DenseEstimator(1024);
  }

  @Test
  public void testRoundtrip() {
    Estimator estimator = getEstimator();

    for (int i = 0; i < estimator.getNumberOfBuckets(); i++) {
      estimator.setIfGreater(i, i % 16);
    }

    DenseEstimator other = new DenseEstimator(estimator.buckets());
    assertEquals(estimator.buckets(), other.buckets());
  }
}
