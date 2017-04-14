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
package com.facebook.stats;

import com.google.common.collect.Lists;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.facebook.logging.Logger;
import com.facebook.logging.LoggerImpl;
import com.facebook.stats.mx.StatsUtil;

import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class TestQuantileDigest {
  private final static Logger LOG = LoggerImpl.getLogger(TestQuantileDigest.class);

  /**
   * This value should be the result of:
   * <p/>
   * use the same value in all cases, result of calling addTestValuesForHistogram on
   * TestingClock testingClock = new TestingClock(System.currentTimeMillis());
   * MultiWindowDistribution multiWindowDistribution = new MultiWindowDistribution(
   * new QuantileDigest(maxError, 0.0, testingClock, false),
   * new QuantileDigest(maxError, 0.0, testingClock, false),
   * new QuantileDigest(maxError, 0.0, testingClock, false),
   * new QuantileDigest(maxError, 0.0, testingClock, false)
   * );
   * <p/>
   * addTestValuesForHistogram(multiWindowDistrobution);
   */
  private static final String HISTOGRAM_VALUE =
    "-1:0:0,0:2:1,3:1:4,6:2:7,9:1:10,12:2:13,15:1:16,18:2:19,21:1:22,24:2:25,27:1:28,30:2:31," +
      "33:1:34,36:2:37,39:1:40,42:2:43,45:1:46,48:2:49,51:1:52,54:2:55,57:1:58,60:2:61,63:1:64," +
      "66:2:67,69:1:70,72:2:73,75:1:76,78:2:79,81:1:82,84:2:85,87:1:88,90:2:91,93:1:94,96:2:97," +
      "99:1:100,102:2:103,105:1:106,108:2:109,111:1:112,114:2:115,117:1:118,120:2:121,123:1:124," +
      "126:2:127,129:1:130,132:2:133,135:1:136,138:2:139,141:1:142,144:2:145,147:1:148,150:2:151," +
      "153:1:154,156:2:157,159:1:160,162:2:163,165:1:166,168:2:169,171:1:172,174:2:175,177:1:178," +
      "180:2:181,183:1:184,186:2:187,189:1:190,192:2:193,195:1:196,198:2:199,201:0:0,204:0:0," +
      "207:0:0,210:0:0,213:0:0,216:0:0,219:0:0,222:0:0,225:0:0,228:0:0,231:0:0,234:0:0,237:0:0," +
      "240:0:0,243:0:0,246:0:0,249:0:0,252:0:0,255:0:0,258:0:0,261:0:0,264:0:0,267:0:0,270:0:0," +
      "273:0:0,276:0:0,279:0:0,282:0:0,285:0:0,288:0:0,291:0:0,294:0:0,297:0:0,200:0:0";

  private MultiWindowDistribution multiWindowDistributionForCounterTests;
  private List<String> periodStringList;
  private MultiWindowDistribution multiWindowDistributionForHistogramTests;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    periodStringList = Arrays.asList(".60", ".600", ".3600", "");

    TestingClock testingClock = new TestingClock(System.currentTimeMillis());
    double maxError = 0.0001;
    multiWindowDistributionForCounterTests = new MultiWindowDistribution(
      new QuantileDigest(maxError, 0.0, testingClock, false),
      new QuantileDigest(maxError, 0.0, testingClock, false),
      new QuantileDigest(maxError, 0.0, testingClock, false),
      new QuantileDigest(maxError, 0.0, testingClock, false)
    );
    multiWindowDistributionForHistogramTests = new MultiWindowDistribution(
      new QuantileDigest(maxError, 0.0, testingClock, false),
      new QuantileDigest(maxError, 0.0, testingClock, false),
      new QuantileDigest(maxError, 0.0, testingClock, false),
      new QuantileDigest(maxError, 0.0, testingClock, false)
    );
    addTestValuesForCounters(multiWindowDistributionForCounterTests);
  }

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

    assertEquals(
      digest.getQuantiles(asList(0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0)),
      asList(0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 9L)
    );
  }

  @Test(groups = "fast")
  public void testHistogramQuery() throws Exception {
    QuantileDigest digest = new QuantileDigest(1);

    addAll(digest, asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));

    // should have no compressions with so few values and the allowed error
    assertEquals(digest.getCompressions(), 0);
    assertEquals(digest.getConfidenceFactor(), 0.0);

    assertEquals(
      digest.getHistogram(asList(0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L)),
      asList(
        new QuantileDigest.Bucket(0, Double.NaN),
        new QuantileDigest.Bucket(1, 0),
        new QuantileDigest.Bucket(1, 1),
        new QuantileDigest.Bucket(1, 2),
        new QuantileDigest.Bucket(1, 3),
        new QuantileDigest.Bucket(1, 4),
        new QuantileDigest.Bucket(1, 5),
        new QuantileDigest.Bucket(1, 6),
        new QuantileDigest.Bucket(1, 7),
        new QuantileDigest.Bucket(1, 8),
        new QuantileDigest.Bucket(1, 9)
      )
    );

    assertEquals(
      digest.getHistogram(asList(7L, 10L)),
      asList(
        new QuantileDigest.Bucket(7, 3),
        new QuantileDigest.Bucket(3, 8)
      )
    );

    // test some edge conditions
    assertEquals(digest.getHistogram(asList(0L)), asList(new QuantileDigest.Bucket(0, Double.NaN)));
    assertEquals(digest.getHistogram(asList(9L)), asList(new QuantileDigest.Bucket(9, 4)));
    assertEquals(digest.getHistogram(asList(10L)), asList(new QuantileDigest.Bucket(10, 4.5)));
    assertEquals(
      digest.getHistogram(asList(Long.MAX_VALUE)),
      asList(new QuantileDigest.Bucket(10, 4.5))
    );
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
    QuantileDigest digest = new QuantileDigest(
      1,
      ExponentialDecay.computeAlpha(0.5, 60),
      clock,
      true
    );

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
    QuantileDigest digest = new QuantileDigest(
      1,
      ExponentialDecay.computeAlpha(0.5, 60),
      clock,
      true
    );

    addAll(digest, asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));

    // should have no compressions with so few values and the allowed error
    assertEquals(digest.getCompressions(), 0);
    assertEquals(digest.getConfidenceFactor(), 0.0);

    clock.increment(60, TimeUnit.SECONDS);
    addAll(digest, asList(10, 11, 12, 13, 14, 15, 16, 17, 18, 19));

    // The first 10 values only contribute 5 to the counts per the alpha factor
    assertEquals(
      digest.getHistogram(asList(10L, 20L)),
      asList(new QuantileDigest.Bucket(5.0, 4.5), new QuantileDigest.Bucket(10.0, 14.5))
    );

    assertEquals(digest.getCount(), 15.0);
  }

  @Test(groups = "fast")
  public void testDecayedCountsWithClockIncrementSmallerThanRescaleThreshold() throws Exception {
    int targetAgeInSeconds = (int) (QuantileDigest.RESCALE_THRESHOLD_SECONDS - 1);

    TestingClock clock = new TestingClock();
    QuantileDigest digest = new QuantileDigest(
      1,
      ExponentialDecay.computeAlpha(0.5, targetAgeInSeconds), clock, false
    );

    addAll(digest, asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));
    clock.increment(targetAgeInSeconds, TimeUnit.SECONDS);
    addAll(digest, asList(10, 11, 12, 13, 14, 15, 16, 17, 18, 19));

    // The first 10 values only contribute 5 to the counts per the alpha factor
    assertEquals(
      digest.getHistogram(asList(10L, 20L)),
      asList(new QuantileDigest.Bucket(5.0, 4.5), new QuantileDigest.Bucket(10.0, 14.5))
    );

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

    QuantileDigest digest = new QuantileDigest(
      0.01,
      ExponentialDecay.computeAlpha(QuantileDigest.ZERO_WEIGHT_THRESHOLD, 60), clock, false
    );

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

    QuantileDigest digest = new QuantileDigest(
      0.01,
      ExponentialDecay.computeAlpha(QuantileDigest.ZERO_WEIGHT_THRESHOLD / 2, targetAgeInSeconds),
      clock, true
    );

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
      "Processed %d entries in %d ms. Insertion rate = %f entries/s",
      count,
      TimeUnit.NANOSECONDS.toMillis(totalTime),
      count / (totalTime * 1.0 / TimeUnit.SECONDS.toNanos(1))
    );

    LOG.info(
      "Compressions: %d, %f entries/compression", digest.getCompressions(),
      digest.getCount() / digest.getCompressions()
    );
  }

  @Test(groups = "fast")
  public void testQuantileDigestPercentileCounters() throws Exception {

    Map<String, Long> counterMap = new HashMap<>();

    StatsUtil.addQuantileToCounters("baseKey", multiWindowDistributionForCounterTests, counterMap);

    Assert.assertEquals(counterMap.size(), 16);

    for (String timePeriod : periodStringList) {
      assertKeyIsValue(counterMap, "baseKey.p50" + timePeriod, 21L);
    }
  }

  @Test(groups = "fast")
  public void testQuantileDigestHistogramExportedValue() throws Exception {
    Map<String, String> exportedValuesMap = new HashMap<>();

    addTestValuesForHistogram(multiWindowDistributionForHistogramTests);
    StatsUtil.addHistogramToExportedValues(
      "baseKey", multiWindowDistributionForHistogramTests, exportedValuesMap
    );

    for (String timePeriod : periodStringList) {
      assertKeyIsValue(exportedValuesMap, "baseKey.hist" + timePeriod, HISTOGRAM_VALUE);
    }
  }

  private <V> void assertKeyIsValue( Map<String, V> resultMap, String key, V value) {
    V computedValue = resultMap.get(key);

    Assert.assertNotNull(computedValue, String.format("key %s should not be null", key));
    Assert.assertEquals(computedValue, value);
  }

  private void addTestValuesForCounters(MultiWindowDistribution multiWindowDistribution) {
    multiWindowDistribution.add(1);
    multiWindowDistribution.add(2);
    multiWindowDistribution.add(10);
    multiWindowDistribution.add(20);
    multiWindowDistribution.add(21);
    multiWindowDistribution.add(100);
    multiWindowDistribution.add(200);
    multiWindowDistribution.add(1000);
    multiWindowDistribution.add(2000);
  }

  private void addTestValuesForHistogram(MultiWindowDistribution multiWindowDistribution) {
    for (int i = 0; i <= 200; i += 2) {
      multiWindowDistribution.add(i);
    }
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
