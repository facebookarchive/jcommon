/*
 * Copyright (C) 2018 Facebook, Inc.
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
package com.facebook.stats.concurrent;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.lang.Long.MAX_VALUE;
import static java.lang.Long.MIN_VALUE;

public class TestSpreadStat {
  @Test
  public void testBasic() {
    SpreadTest test = new SpreadTest();

    // 0th bucket (stats get dropped on 1 second boundaries)
    {
      // start offset: 0.123
      test.assertSum(0, 0, 0, 0);
      test.assertRate(0, 0, 0, 0);
      test.assertMin(MAX_VALUE, MAX_VALUE, MAX_VALUE, MAX_VALUE);
      test.assertMax(MIN_VALUE, MIN_VALUE, MIN_VALUE, MIN_VALUE);
      test.assertAverage(0, 0, 0, 0);
      test.assertSamples(0, 0, 0, 0);
      test.advanceMillis(100);
      // elapsed: 0.223
      test.update(100);
      test.assertSum(100, 100, 100, 100);
      test.assertRate(100, 100, 100, 100);
      test.assertMin(100, 100, 100, 100);
      test.assertMax(100, 100, 100, 100);
      test.assertAverage(100, 100, 100, 100);
      test.assertSamples(1, 1, 1, 1);
      test.advanceMillis(456);
      // elapsed: 0.679
      test.update(100);
      test.assertSum(200, 200, 200, 200);
      test.assertRate(200, 200, 200, 200);
      test.assertMin(100, 100, 100, 100);
      test.assertMax(100, 100, 100, 100);
      test.assertAverage(100, 100, 100, 100);
      test.assertSamples(2, 2, 2, 2);
      test.advanceMillis(500);
      // elapsed: 1.179
    } // bucket size: 200

    // 1st bucket
    {
      test.assertSum(200, 200, 200, 200);
      test.assertRate(200, 200, 200, 200);
      test.assertMin(100, 100, 100, 100);
      test.assertMax(100, 100, 100, 100);
      test.assertAverage(100, 100, 100, 100);
      test.assertSamples(2, 2, 2, 2);
      test.advanceMillis(100);
      // elapsed: 1.279
      test.assertSum(200, 200, 200, 200);
      test.assertRate(200, 200, 200, 200);
      test.assertMax(100, 100, 100, 100);
      test.assertAverage(100, 100, 100, 100);
      test.assertSamples(2, 2, 2, 2);
      test.advanceMillis(1_200);
      // elapsed: 2.479
    } // bucket size: 0

    // 2nd bucket
    {
      test.assertSum(200, 200, 200, 200);
      test.assertRate(100, 100, 100, 100);
      test.assertMax(100, 100, 100, 100);
      test.assertAverage(100, 100, 100, 100);
      test.assertSamples(2, 2, 2, 2);
      test.advanceMillis(202);
      // elapsed: 2.681
      test.update(123);
      test.advanceMillis(3);
      test.assertStats();
      // elapsed: 2.684
      test.update(1);
      test.advanceMillis(5);
      test.assertStats();
      // elapsed: 2.689
      test.update(2);
      test.advanceMillis(12);
      test.assertStats();
      // elapsed: 2.701
      test.update(1);
      test.advanceMillis(1_300);
      test.assertStats();
      // elapsed: 4.001
    } // bucket size: 127

    // 4th bucket (3rd bucket is empty)
    {
      test.update(111);
      test.advanceMillis(57_200);
      test.assertStats();
      // elapsed: 61.201
    } // bucket size: 111

    // 61st bucket
    {
      test.assertSum(238, 438, 438, 438);
      test.assertRate(3, 7, 7, 7);
      test.assertMax(123, 123, 123, 123);
      test.assertAverage(47, 62, 62, 62);
      test.assertSamples(5, 7, 7, 7);
      test.update(53);
      test.advanceMillis(1_802);
      // elapsed: 63.300
    } // bucket size: 0

    // 63rd bucket
    {
      test.assertSum(164, 491, 491, 491);
      test.assertRate(2, 7, 7, 7);
      test.assertMax(111, 123, 123, 123);
      test.assertAverage(82, 61, 61, 61);
      test.assertSamples(2, 8, 8, 8);
    }
  }

  @Test
  public void testLongRunningSmallSteps() {
    SpreadTest test = new SpreadTest();
    Random random = new Random(0);

    for (int i = 1; i < 5_000; ++i) {
      int value = random.nextBoolean() ? 0 : random.nextInt(100);

      if (value > 0 || random.nextBoolean()) {
        test.update(value);
      }

      test.assertStats();
      test.advanceMillis(random.nextInt(100));
    }
  }

  @Test
  public void testLongRunningBigSteps() {
    SpreadTest test = new SpreadTest();
    Random random = new Random(0);

    for (int i = 1; i < 5_000; ++i) {
      int value = random.nextBoolean() ? 0 : random.nextInt(100);

      if (value > 0 || random.nextBoolean()) {
        test.update(value);
      }

      test.assertStats();
      test.advanceMillis(random.nextInt(5_000));
    }
  }

  @Test
  public void testTrivialLongRunning() {
    SpreadTest test = new SpreadTest();

    for (int i = 1; i < 10_000; ++i) {
      test.advanceMillis(1_000);
      test.update(10);
      test.assertSum(10 * Math.min(i, 60), 10 * Math.min(i, 600), 10 * Math.min(i, 3600), 10 * i);
      test.assertRate(10, 10, 10, 10);
      test.assertMax(10, 10, 10, 10);
      test.assertAverage(10, 10, 10, 10);
      test.assertSamples(Math.min(i, 60), Math.min(i, 600), Math.min(i, 3600), i);
    }
  }

  @Test
  public void testDelayAfterSustained() {
    // specifically tests the cause of #5107546, where the offset was incorrectly reset if a second
    // was skipped after a prolonged period of constant increments
    SpreadTest test = new SpreadTest();

    for (int i = 0; i < 10_000; ++i) {
      test.advanceMillis(1_000);
      test.update(10);
    }

    test.assertSum(600, 6000, 36_000, 100_000);
    test.advanceMillis(10_000);
    test.assertSum(500, 5900, 35_900, 100_000);
  }

  private static class SpreadTest {
    private static final ZonedDateTime START = ZonedDateTime.parse("2014-08-22T01:02:03.123Z");

    private final List<Sample> samples = new ArrayList<>(50_000);
    private final MockClock clock = new MockClock(START);
    private final SpreadStat spreadStat = new SpreadStat("spread-test", clock);

    void update(long value) {
      Sample sample = new Sample(clock.instant().getEpochSecond(), value);

      samples.add(sample);
      spreadStat.update(value);
    }

    void advanceMillis(int millis) {
      clock.advanceMillis(millis);
    }

    void assertStats() {
      assertStats(60);
      assertStats(600);
      assertStats(3600);
      assertStats(null);
    }

    void assertSum(long minute, long tenMinute, long hour, long allTime) {
      Snapshot actual = spreadStat.getSum();
      Snapshot expected = new Snapshot("sum", allTime, hour, tenMinute, minute);

      Assert.assertEquals(actual, expected, "sum @ " + elapsedSeconds());
    }

    void assertRate(long minute, long tenMinute, long hour, long allTime) {
      Snapshot actual = spreadStat.getRate();
      Snapshot expected = new Snapshot("rate", allTime, hour, tenMinute, minute);

      Assert.assertEquals(actual, expected, "rate @ " + elapsedSeconds());
    }

    void assertSamples(long minute, long tenMinute, long hour, long allTime) {
      Snapshot actual = spreadStat.getSamples();
      Snapshot expected = new Snapshot("samples", allTime, hour, tenMinute, minute);

      Assert.assertEquals(actual, expected, "samples @ " + elapsedSeconds());
    }

    void assertAverage(long minute, long tenMinute, long hour, long allTime) {
      Snapshot actual = spreadStat.getAverage();
      Snapshot expected = new Snapshot("average", allTime, hour, tenMinute, minute);

      Assert.assertEquals(actual, expected, "average @ " + elapsedSeconds());
    }

    void assertMin(long minute, long tenMinute, long hour, long allTime) {
      Snapshot actual = spreadStat.getMin();
      Snapshot expected = new Snapshot("min", allTime, hour, tenMinute, minute);

      Assert.assertEquals(actual, expected, "min @ " + elapsedSeconds());
    }

    void assertMax(long minute, long tenMinute, long hour, long allTime) {
      Snapshot actual = spreadStat.getMax();
      Snapshot expected = new Snapshot("max", allTime, hour, tenMinute, minute);

      Assert.assertEquals(actual, expected, "max @ " + elapsedSeconds());
    }

    private long elapsedSeconds() {
      return (clock.millis() - START.toInstant().toEpochMilli()) / 1000;
    }

    private void assertStats(Integer secondsAgo) {
      long now = clock.instant().getEpochSecond();
      long elapsedSeconds = now - START.toInstant().getEpochSecond();
      String suffix = "";
      Stream<Sample> samples = this.samples.stream();

      if (secondsAgo != null) {
        long then = now - secondsAgo;

        if (elapsedSeconds > secondsAgo) {
          elapsedSeconds = secondsAgo;
        }

        suffix = "." + secondsAgo;
        samples = samples.filter(sample -> sample.getSeconds() > then);
      }

      if (elapsedSeconds == 0) {
        elapsedSeconds = 1;
      }

      long min = MAX_VALUE;
      long max = MIN_VALUE;
      long sum = 0;
      long size = 0;
      Iterator<Sample> iterator = samples.iterator();

      while (iterator.hasNext()) {
        Sample sample = iterator.next();
        long value = sample.getValue();

        if (value < min) {
          min = value;
        }

        if (value > max) {
          max = value;
        }

        sum += value;
        ++size;
      }

      Function<Snapshot, Long> accessor = null;

      if (secondsAgo == null) {
        accessor = Snapshot::getAllTime;
      } else if (secondsAgo == 60) {
        accessor = Snapshot::getMinute;
      } else if (secondsAgo == 600) {
        accessor = Snapshot::getTenMinute;
      } else if (secondsAgo == 3600) {
        accessor = Snapshot::getHour;
      } else {
        Assert.fail("Bogus secondsAgo: " + secondsAgo);
      }

      assertCounter("sum" + suffix, accessor.apply(spreadStat.getSum()), sum);
      assertCounter("samples" + suffix, accessor.apply(spreadStat.getSamples()), size);
      assertCounter("min" + suffix, accessor.apply(spreadStat.getMin()), min);
      assertCounter("max" + suffix, accessor.apply(spreadStat.getMax()), max);
      assertCounter("avg" + suffix, accessor.apply(spreadStat.getAverage()), sum / size);
      assertCounter("rate" + suffix, accessor.apply(spreadStat.getRate()), sum / elapsedSeconds);
    }

    private void assertCounter(String name, Long actual, Long expected) {
      Assert.assertEquals(
        actual,
        expected,
        name + " @ " + (clock.instant().getEpochSecond() - START.toInstant().getEpochSecond())
      );
    }
  }

  private static class Sample {
    private final long second;
    private final long value;

    Sample(long second, long value) {
      this.second = second;
      this.value = value;
    }

    long getSeconds() {
      return second;
    }

    long getValue() {
      return value;
    }

    @Override
    public String toString() {
      return (second - SpreadTest.START.toInstant().getEpochSecond()) + ": " + value;
    }
  }
}
