package com.facebook.stats.cardinality;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class TestAdaptiveHyperLogLog {
  @Test
  public void testConsistencyWithFixedHyperLogLog() {
    HyperLogLog simple = new HyperLogLog(1024);
    AdaptiveHyperLogLog adaptive = new AdaptiveHyperLogLog(1024);

    for (int i = 0; i < 4000; ++i) {
      simple.add(i);
      adaptive.add(i);

      assertEquals(adaptive.estimate(), simple.estimate());
      assertEquals(adaptive.buckets(), simple.buckets());
    }
  }

  @Test
  public void testRoundtripLowCardinality() {
    AdaptiveHyperLogLog expected = new AdaptiveHyperLogLog(1024);
    for (int i = 0; i < 10; ++i) {
      expected.add(i);
    }

    AdaptiveHyperLogLog actual = new AdaptiveHyperLogLog(expected.buckets());

    assertEquals(actual.buckets(), expected.buckets());
    assertEquals(actual.estimate(), expected.estimate());
  }

  @Test
  public void testRoundtripHighCardinality() {
    AdaptiveHyperLogLog expected = new AdaptiveHyperLogLog(1024);
    for (int i = 0; i < 30000; ++i) {
      expected.add(i);
    }

    AdaptiveHyperLogLog actual = new AdaptiveHyperLogLog(expected.buckets());

    assertEquals(actual.buckets(), expected.buckets());
    assertEquals(actual.estimate(), expected.estimate());
  }

  @Test
  public void testMergeNoOverlap() {
    int buckets = 1024;
    AdaptiveHyperLogLog first = new AdaptiveHyperLogLog(buckets);
    AdaptiveHyperLogLog second = new AdaptiveHyperLogLog(buckets);

    int count = 30000;
    int value = 0;
    for (int i = 0; i < count; i++) {
      first.add(++value);
    }
    for (int i = 0; i < count; i++) {
      second.add(++value);
    }

    AdaptiveHyperLogLog merged = AdaptiveHyperLogLog.merge(first, second);

    assertEstimate(merged.estimate(), count, buckets);
  }

  @Test
  public void testMergeWithOverlap()
  {
    int buckets = 1024;
    AdaptiveHyperLogLog first = new AdaptiveHyperLogLog(buckets);
    AdaptiveHyperLogLog second = new AdaptiveHyperLogLog(buckets);

    int count = 30000;
    for (int i = 0; i < 2 * count / 3; i++) {
      first.add(count);
    }
    for (int i = count / 3; i < count; i++) {
      second.add(count);
    }

    AdaptiveHyperLogLog merged = AdaptiveHyperLogLog.merge(first, second);

    assertEstimate(merged.estimate(), count, buckets);
  }

  @Test
  public void testMergeInPlace() {
    int buckets = 1024;
    AdaptiveHyperLogLog first = new AdaptiveHyperLogLog(buckets);
    AdaptiveHyperLogLog second = new AdaptiveHyperLogLog(buckets);

    int count = 30000;
    int value = 0;
    for (int i = 0; i < count; i++) {
      first.add(++value);
    }
    for (int i = 0; i < count; i++) {
      second.add(++value);
    }

    first.merge(second);

    assertEstimate(first.estimate(), count, buckets);
  }

  @Test
  public void testAddSameElements()
  {
    AdaptiveHyperLogLog estimator = new AdaptiveHyperLogLog(1024);

    for (int i = 0; i < 10000; i++) {
      estimator.add(i);
    }

    long expectedEstimate = estimator.estimate();

    for (int i = 0; i < 10000; i++) {
      assertFalse(estimator.add(i));
    }

    assertEquals(estimator.estimate(), expectedEstimate);
  }

  private void assertEstimate(long actual, int expected, int numberOfBuckets) {
    // this is actually the standard deviation of the expected error, but it provides a
    // good bound for our deterministic test
    double expectedError = 1.04 / Math.sqrt(numberOfBuckets);
    assertTrue((actual - 2 * expected) / (2 * expected) < expectedError);
  }

}
