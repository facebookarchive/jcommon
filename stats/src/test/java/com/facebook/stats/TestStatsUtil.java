package com.facebook.stats;

import com.facebook.stats.mx.Stats;
import com.facebook.util.TimeUtil;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.joda.time.Duration;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper methods for converting stat objects into key/value pairs that conform to standing
 * naming conventions around <type>.<time_window>
 */
public class TestStatsUtil {
  private Stats stats;
  private Map<String, Long> countersMap;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    stats = new Stats();
    countersMap = new HashMap<String, Long>();
  }

  @Test(groups = "fast")
  public void testRateAndSum() throws Exception {
    int expectedRatePerSecond = 10;
    int numHours = 2;
    long totalSeconds = Duration.standardHours(numHours).getStandardSeconds();
    long expectedTotal = totalSeconds * expectedRatePerSecond;

    TimeUtil.setNow(new DateTime("2012-01-01"));
    // generate rate using second granularity
    for (int i = 0; i < totalSeconds; i++) {
      stats.incrementRate("rate1", expectedRatePerSecond);
      TimeUtil.advanceNow(Duration.standardSeconds(1));
    }

    assertCounterValue("rate1.sum.60", 60 * expectedRatePerSecond);
    assertCounterValue("rate1.sum.600", 600 * expectedRatePerSecond);
    assertCounterValue("rate1.sum.3600", 3600 * expectedRatePerSecond);
    assertCounterValue("rate1.sum", expectedTotal);

    assertCounterValue("rate1.rate.60", expectedRatePerSecond);
    assertCounterValue("rate1.rate.600", expectedRatePerSecond);
    assertCounterValue("rate1.rate.3600", expectedRatePerSecond);
    assertCounterValue("rate1.rate", expectedRatePerSecond);
  }

  @Test(groups = "fast")
  public void testCount() throws Exception {
    stats.incrementCounter("count", 1);
    assertCounterValue("count", 1);
  }

  @Test(groups = "fast")
  public void testSpread() throws Exception {
    stats.incrementSpread("spread", 1);
    stats.incrementSpread("spread", 10);
    assertCounterValue("spread.samples.60", 2);
    assertCounterValue("spread.min.60", 1);
    assertCounterValue("spread.avg.60", 5);
    assertCounterValue("spread.max.60", 10);
    assertCounterValue("spread.samples.600", 2);
    assertCounterValue("spread.min.600", 1);
    assertCounterValue("spread.avg.600", 5);
    assertCounterValue("spread.max.600", 10);
    assertCounterValue("spread.samples.3600", 2);
    assertCounterValue("spread.min.3600", 1);
    assertCounterValue("spread.avg.3600", 5);
    assertCounterValue("spread.max.3600", 10);
    assertCounterValue("spread.samples", 2);
    assertCounterValue("spread.min", 1);
    assertCounterValue("spread.avg", 5);
    assertCounterValue("spread.max", 10);
  }

  private void assertCounterValue(String key, long value) {
    if (countersMap.isEmpty()) {
      stats.exportCounters(countersMap);
    }

    Assert.assertTrue(countersMap.containsKey(key), String.format("missing key %s", key));
    Assert.assertEquals(countersMap.get(key).longValue(), value);
  }

  @AfterMethod(alwaysRun = true)
  public void tearDown() throws Exception {
    DateTimeUtils.setCurrentMillisSystem();
  }
}
