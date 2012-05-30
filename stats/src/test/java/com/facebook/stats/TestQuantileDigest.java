package com.facebook.stats;

import com.facebook.logging.Logger;
import com.facebook.logging.LoggerImpl;
import com.google.common.collect.Lists;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class TestQuantileDigest {

  private final static Logger LOG = LoggerImpl.getLogger(TestQuantileDigest.class);

  @Test(groups = "fast")
  public void testSingleAdd() {
    QuantileDigest digest = new QuantileDigest(1);
    digest.add(0);

    digest.validate();

    // should have no compressions with so few values and the allowed error
    assertEquals(digest.getCompressions(), 0);

    assertEquals(digest.getCount(), (double) 1);
    assertEquals(digest.getNonZeroNodeCount(), 1);
    assertEquals(digest.getTotalNodeCount(), 1);
  }

  @Test(groups = "fast")
  public void testRepeatedValue() {
    QuantileDigest digest = new QuantileDigest(1);
    digest.add(0);
    digest.add(0);

    digest.validate();

    // should have no compressions with so few values and the allowed error
    assertEquals(digest.getCompressions(), 0);

    assertEquals(digest.getCount(), (double) 2);
    assertEquals(digest.getNonZeroNodeCount(), 1);
    assertEquals(digest.getTotalNodeCount(), 1);
  }

  @Test(groups = "fast")
  public void testTwoDistinctValues() {
    QuantileDigest digest = new QuantileDigest(1);
    digest.add(0);
    digest.add(Long.MAX_VALUE);

    digest.validate();

    // should have no compressions with so few values and the allowed error
    assertEquals(digest.getCompressions(), 0);

    assertEquals(digest.getCount(), (double) 2);
    assertEquals(digest.getNonZeroNodeCount(), 2);
    assertEquals(digest.getTotalNodeCount(), 3);
  }

  @Test(groups = "fast")
  public void testTreeBuilding() {
    QuantileDigest digest = new QuantileDigest(1);

    List<Integer> values = asList(0, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 4, 5, 6, 7);
    addAll(digest, values);

    // should have no compressions with so few values and the allowed error
    assertEquals(digest.getCompressions(), 0);
    assertEquals(digest.getConfidenceFactor(), 0.0);

    assertEquals(digest.getCount(), (double) values.size());
    assertEquals(digest.getNonZeroNodeCount(), 7);
    assertEquals(digest.getTotalNodeCount(), 13);
  }

  @Test(groups = "fast")
  public void testTreeBuildingReverse() {
    QuantileDigest digest = new QuantileDigest(1);

    List<Integer> values = asList(0, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 4, 5, 6, 7);
    addAll(digest, Lists.reverse(values));

    // should have no compressions with so few values and the allowed error
    assertEquals(digest.getCompressions(), 0);
    assertEquals(digest.getConfidenceFactor(), 0.0);

    assertEquals(digest.getCount(), (double) values.size());
    assertEquals(digest.getNonZeroNodeCount(), 7);
    assertEquals(digest.getTotalNodeCount(), 13);
  }


  @Test(groups = "fast")
  public void testBasicCompression() {
    // maxError = 0.8 so that we get compression factor = 5 with the data below
    QuantileDigest digest = new QuantileDigest(0.8, 0, new TestingClock(), false);

    List<Integer> values = asList(0, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 4, 5, 6, 7);
    addAll(digest, values);

    digest.compress();
    digest.validate();

    assertEquals(digest.getCount(), (double) values.size());
    assertEquals(digest.getNonZeroNodeCount(), 5);
    assertEquals(digest.getTotalNodeCount(), 7);
    assertEquals(digest.getConfidenceFactor(), 0.2);
  }

  @Test(groups = "fast")
  public void testCompression() throws Exception {
    QuantileDigest digest = new QuantileDigest(1, 0, new TestingClock(), false);

    for (int loop = 0; loop < 2; ++loop) {
      addRange(digest, 0, 15);

      digest.compress();
      digest.validate();
    }
  }

  @Test(groups = "fast")
  public void testQuantile() {
    QuantileDigest digest = new QuantileDigest(1);

    addAll(digest, asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));

    // should have no compressions with so few values and the allowed error
    assertEquals(digest.getCompressions(), 0);
    assertEquals(digest.getConfidenceFactor(), 0.0);

    assertEquals(digest.getQuantile(0.0), 0);
    assertEquals(digest.getQuantile(0.1), 1);
    assertEquals(digest.getQuantile(0.2), 2);
    assertEquals(digest.getQuantile(0.3), 3);
    assertEquals(digest.getQuantile(0.4), 4);
    assertEquals(digest.getQuantile(0.5), 5);
    assertEquals(digest.getQuantile(0.6), 6);
    assertEquals(digest.getQuantile(0.7), 7);
    assertEquals(digest.getQuantile(0.8), 8);
    assertEquals(digest.getQuantile(0.9), 9);
    assertEquals(digest.getQuantile(1), 9);
  }

  @Test(groups = "fast")
  public void testBatchQuantileQuery() throws Exception {
    QuantileDigest digest = new QuantileDigest(1);

    addAll(digest, asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));

    // should have no compressions with so few values and the allowed error
    assertEquals(digest.getCompressions(), 0);
    assertEquals(digest.getConfidenceFactor(), 0.0);

    assertEquals(digest.getQuantiles(asList(0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0)),
                 asList(0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 9L));
  }

  @Test(groups = "fast")
  public void testHistogramQuery() throws Exception {
    QuantileDigest digest = new QuantileDigest(1);

    addAll(digest, asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));

    // should have no compressions with so few values and the allowed error
    assertEquals(digest.getCompressions(), 0);
    assertEquals(digest.getConfidenceFactor(), 0.0);

    assertEquals(digest.getHistogram(asList(0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L)),
                 asList(new QuantileDigest.Bucket(0, Double.NaN),
                        new QuantileDigest.Bucket(1, 0),
                        new QuantileDigest.Bucket(1, 1),
                        new QuantileDigest.Bucket(1, 2),
                        new QuantileDigest.Bucket(1, 3),
                        new QuantileDigest.Bucket(1, 4),
                        new QuantileDigest.Bucket(1, 5),
                        new QuantileDigest.Bucket(1, 6),
                        new QuantileDigest.Bucket(1, 7),
                        new QuantileDigest.Bucket(1, 8),
                        new QuantileDigest.Bucket(1, 9)));

    assertEquals(digest.getHistogram(asList(7L, 10L)),
                 asList(new QuantileDigest.Bucket(7, 3),
                        new QuantileDigest.Bucket(3, 8)));

    // test some edge conditions
    assertEquals(digest.getHistogram(asList(0L)), asList(new QuantileDigest.Bucket(0, Double.NaN)));
    assertEquals(digest.getHistogram(asList(9L)), asList(new QuantileDigest.Bucket(9, 4)));
    assertEquals(digest.getHistogram(asList(10L)), asList(new QuantileDigest.Bucket(10, 4.5)));
    assertEquals(digest.getHistogram(asList(Long.MAX_VALUE)),
                 asList(new QuantileDigest.Bucket(10, 4.5)));
  }

  @Test(groups = "fast")
  public void testHistogramQueryAfterCompression() throws Exception {
    QuantileDigest digest = new QuantileDigest(0.1);

    int total = 10000;
    addRange(digest, 0, total);

    // compression should've run at this error rate and count
    assertTrue(digest.getCompressions() > 0);

    double actualMaxError = digest.getConfidenceFactor();

    for (long value = 0; value < total; ++value) {
      QuantileDigest.Bucket bucket = digest.getHistogram(asList(value)).get(0);

      // estimated count should have an absolute error smaller than 2 * maxError * N
      assertTrue(Math.abs(bucket.getCount() - value) < 2 * actualMaxError * total);
    }
  }


  @Test(groups = "fast")
  public void testQuantileQueryError() {
    double maxError = 0.1;

    QuantileDigest digest = new QuantileDigest(maxError);

    int count = 10000;
    addRange(digest, 0, count);

    // compression should've run at this error rate and count
    assertTrue(digest.getCompressions() > 0);

    assertTrue(digest.getConfidenceFactor() > 0);
    assertTrue(digest.getConfidenceFactor() < maxError);

    for (int value = 0; value < count; ++value) {
      double quantile = value * 1.0 / count;
      long estimatedValue = digest.getQuantile(quantile);

      // true rank of estimatedValue is == estimatedValue because
      // we've inserted a list of ordered numbers starting at 0
      double error = Math.abs(estimatedValue - quantile * count) * 1.0 / count;

      assertTrue(error < maxError);
    }
  }

  @Test(groups = "fast")
  public void testDecayedQuantiles() throws Exception {
    TestingClock clock = new TestingClock();
    QuantileDigest digest = new QuantileDigest(1, ExponentialDecay.computeAlpha(0.5, 60), clock, true);

    addAll(digest, asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));

    // should have no compressions with so few values and the allowed error
    assertEquals(digest.getCompressions(), 0);
    assertEquals(digest.getConfidenceFactor(), 0.0);

    clock.increment(60, TimeUnit.SECONDS);
    addAll(digest, asList(10, 11, 12, 13, 14, 15, 16, 17, 18, 19));

    // Considering that the first 10 values now have a weight of 0.5 per the alpha factor, they only contributed a count
    // of 5 to rank computations. Therefore, the 50th percentile is equivalent to a weighted rank of (5 + 10) / 2 = 7.5,
    // which corresponds to value 12
    assertEquals(digest.getQuantile(0.5), 12);
  }

  @Test(groups = "fast")
  public void testDecayedCounts() throws Exception {
    TestingClock clock = new TestingClock();
    QuantileDigest digest = new QuantileDigest(1, ExponentialDecay.computeAlpha(0.5, 60), clock, true);

    addAll(digest, asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));

    // should have no compressions with so few values and the allowed error
    assertEquals(digest.getCompressions(), 0);
    assertEquals(digest.getConfidenceFactor(), 0.0);

    clock.increment(60, TimeUnit.SECONDS);
    addAll(digest, asList(10, 11, 12, 13, 14, 15, 16, 17, 18, 19));

    // The first 10 values only contribute 5 to the counts per the alpha factor
    assertEquals(
        digest.getHistogram(asList(10L, 20L)),
        asList(new QuantileDigest.Bucket(5.0, 4.5), new QuantileDigest.Bucket(10.0, 14.5)));

    assertEquals(digest.getCount(), 15.0);
  }

  @Test(groups = "fast")
  public void testDecayedCountsWithClockIncrementSmallerThanRescaleThreshold() throws Exception {
    int targetAgeInSeconds = (int) (QuantileDigest.RESCALE_THRESHOLD_SECONDS - 1);

    TestingClock clock = new TestingClock();
    QuantileDigest digest = new QuantileDigest(1,
      ExponentialDecay.computeAlpha(0.5, targetAgeInSeconds), clock, false);

    addAll(digest, asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));
    clock.increment(targetAgeInSeconds, TimeUnit.SECONDS);
    addAll(digest, asList(10, 11, 12, 13, 14, 15, 16, 17, 18, 19));

    // The first 10 values only contribute 5 to the counts per the alpha factor
    assertEquals(
        digest.getHistogram(asList(10L, 20L)),
        asList(new QuantileDigest.Bucket(5.0, 4.5), new QuantileDigest.Bucket(10.0, 14.5)));

    assertEquals(digest.getCount(), 15.0);
  }

  @Test(groups = "fast")
  public void testMinMax() throws Exception {
    QuantileDigest digest = new QuantileDigest(0.01, 0, new TestingClock(), false);

    int from = 500;
    int to = 700;
    addRange(digest, from, to + 1);

    assertEquals(digest.getMin(), from);
    assertEquals(digest.getMax(), to);
  }

  @Test(groups = "fast")
  public void testMinMaxWithDecay() throws Exception {
    TestingClock clock = new TestingClock();

    QuantileDigest digest = new QuantileDigest(0.01,
      ExponentialDecay.computeAlpha(QuantileDigest.ZERO_WEIGHT_THRESHOLD, 60), clock, false);

    addRange(digest, 1, 10);

    clock.increment(1000, TimeUnit.SECONDS); // TODO: tighter bounds?

    int from = 4;
    int to = 7;
    addRange(digest, from, to + 1);

    digest.validate();

    assertEquals(digest.getMin(), from);
    assertEquals(digest.getMax(), to);
  }

  @Test(groups = "fast")
  public void testRescaleWithDecayKeepsCompactTree() throws Exception {
    TestingClock clock = new TestingClock();
    int targetAgeInSeconds = (int) (QuantileDigest.RESCALE_THRESHOLD_SECONDS);

    QuantileDigest digest = new QuantileDigest(0.01,
      ExponentialDecay.computeAlpha(QuantileDigest.ZERO_WEIGHT_THRESHOLD / 2, targetAgeInSeconds),
      clock, true);

    for (int i = 0; i < 10; ++i) {
      digest.add(i);
      digest.validate();

      // bump the clock to make all previous values decay to ~0
      clock.increment(targetAgeInSeconds, TimeUnit.SECONDS);
    }

    assertEquals(digest.getTotalNodeCount(), 1);
  }

  @Test(groups = "slow")
  public void testTiming() {
    QuantileDigest digest = new QuantileDigest(0.01, 0, new TestingClock(), true);

    Random random = new Random();

    long totalTime = 0;
    long count = 0;
    long begin = System.nanoTime();
    while (System.nanoTime() - begin < TimeUnit.SECONDS.toNanos(5)) {

      long start = System.nanoTime();
      digest.add(Math.abs(random.nextInt(100000)));

      if (System.nanoTime() - begin > TimeUnit.SECONDS.toNanos(1)) {
        // discard the first second of measurements
        totalTime += System.nanoTime() - start;
        ++count;
      }
    }
    digest.validate();

    LOG.info(
      "Processed %s entries in %s ms. Insertion rate = %s entries/s",
      count,
      TimeUnit.NANOSECONDS.toMillis(totalTime),
      count / (totalTime * 1.0 / TimeUnit.SECONDS.toNanos(1))
    );

    LOG.info(
      "Compressions: %s, %s entries/compression", digest.getCompressions(),
      digest.getCount() / digest.getCompressions()
    );
  }

  private void addAll(QuantileDigest digest, List<Integer> values) {
    for (int value : values) {
      digest.add(value);
    }
    digest.validate();
  }

  private void addRange(QuantileDigest digest, int from, int to) {
    for (int i = from; i < to; ++i) {
      digest.add(i);
    }
    digest.validate();
  }
}
