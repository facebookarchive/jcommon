package com.facebook.stats.cardinality;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

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

}
