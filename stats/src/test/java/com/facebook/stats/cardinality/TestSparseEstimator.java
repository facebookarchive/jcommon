package com.facebook.stats.cardinality;

import org.testng.annotations.Test;

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
}
