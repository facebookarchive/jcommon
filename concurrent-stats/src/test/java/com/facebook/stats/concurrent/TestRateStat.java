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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.ZonedDateTime;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;

public class TestRateStat {
  private static final ZonedDateTime START = ZonedDateTime.parse("2014-08-22T01:02:03.123Z");

  private MockClock clock;

  @BeforeMethod(alwaysRun = true)
  private void setUp() {
    clock = new MockClock(START);
  }

  @Test
  public void testBasic() {
    RateStat rateStat = new RateStat("rs-test", clock);

    // 0th bucket (stats get dropped on 1 second boundaries)
    {
      // start offset: 0.123
      assertSum(rateStat, 0, 0, 0, 0);
      assertRate(rateStat, 0, 0, 0, 0);
      clock.advanceMillis(100);
      // elapsed: 0.223
      rateStat.update(100);
      assertSum(rateStat, 100, 100, 100, 100);
      assertRate(rateStat, 100, 100, 100, 100);
      clock.advanceMillis(456);
      // elapsed: 0.679
      rateStat.update(100);
      assertSum(rateStat, 200, 200, 200, 200);
      assertRate(rateStat, 200, 200, 200, 200);
      clock.advanceMillis(500);
      // elapsed: 1.179
    } // bucket size: 200

    // 1st bucket
    {
      assertSum(rateStat, 200, 200, 200, 200);
      assertRate(rateStat, 200, 200, 200, 200);
      clock.advanceMillis(100);
      // elapsed: 1.279
      assertSum(rateStat, 200, 200, 200, 200);
      assertRate(rateStat, 200, 200, 200, 200);
      clock.advanceMillis(1_200);
      // elapsed: 2.479
    } // bucket size: 0

    // 2nd bucket
    {
      assertSum(rateStat, 200, 200, 200, 200);
      assertRate(rateStat, 100, 100, 100, 100);
      clock.advanceMillis(202);
      // elapsed: 2.681
      rateStat.update(123);
      clock.advanceMillis(3);
      // elapsed: 2.684
      rateStat.update(1);
      clock.advanceMillis(5);
      // elapsed: 2.689
      rateStat.update(2);
      clock.advanceMillis(12);
      // elapsed: 2.701
      rateStat.update(1);
      clock.advanceMillis(1_300);
      // elapsed: 4.001
    } // bucket size: 127

    // 4th bucket (3rd bucket is empty)
    {
      rateStat.update(111);
      clock.advanceMillis(57_200);
      // elapsed: 61.201
    } // bucket size: 111

    // 61st bucket
    {
      assertSum(rateStat, 238, 438, 438, 438);
      assertRate(rateStat, 3, 7, 7, 7);
      rateStat.update(53);
      clock.advanceMillis(1_802);
      // elapsed: 63.300
    } // bucket size: 0

    // 63rd bucket
    {
      assertSum(rateStat, 164, 491, 491, 491);
      assertRate(rateStat, 2, 7, 7, 7);
    }
  }

  @Test
  public void testLongRunning() {
    RateStat rateStat = new RateStat("rs-test", clock);
    Random random = new Random(0);
    Deque<Integer> values = new ArrayDeque<>(10_000);

    for (int i = 0; i < 10_000; ++i) {
      int value = random.nextBoolean() ? 0 : random.nextInt(100);

      if (value > 0 || random.nextBoolean()) {
        rateStat.update(value);
      }

      values.addFirst(value);
      assertSum(
        rateStat,
        values.stream().limit(60).mapToLong(Integer::longValue).sum(),
        values.stream().limit(600).mapToLong(Integer::longValue).sum(),
        values.stream().limit(3600).mapToLong(Integer::longValue).sum(),
        values.stream().mapToLong(Integer::valueOf).sum()
      );
      clock.advanceMillis(1_000);
    }
  }

  @Test
  public void testTrivialLongRunning() {
    RateStat rateStat = new RateStat("rs-test", clock);

    for (int i = 1; i < 10_000; ++i) {
      clock.advanceMillis(1_000);
      rateStat.update(10);
      assertSum(
        rateStat,
        10 * Math.min(i, 60),
        10 * Math.min(i, 600),
        10 * Math.min(i, 3600),
        10 * i
      );
      assertRate(rateStat, 10, 10, 10, 10);
    }
  }

  @Test
  public void testDelayAfterSustained() {
    // specifically tests the cause of #5107546, where the offset was incorrectly reset if a second
    // was skipped after a prolonged period of constant increments
    RateStat rateStat = new RateStat("rs-test", clock);

    for (int i = 0; i < 10_000; ++i) {
      clock.advanceMillis(1_000);
      rateStat.update(10);
    }

    assertSum(rateStat, 600, 6000, 36_000, 100_000);
    clock.advanceMillis(10_000);
    assertSum(rateStat, 500, 5900, 35_900, 100_000);
  }

  private void assertSum(RateStat rateStat, long minute, long tenMinute, long hour, long allTime) {
    Snapshot actual = rateStat.getSum();
    Snapshot expected = new Snapshot("sum", allTime, hour, tenMinute, minute);

    Assert.assertEquals(
      actual, expected, "sum @ " + ((clock.millis() - START.toInstant().toEpochMilli()) / 1000)
    );
  }

  private void assertRate(RateStat rateStat, long minute, long tenMinute, long hour, long allTime) {
    Snapshot actual = rateStat.getRate();
    Snapshot expected = new Snapshot("rate", allTime, hour, tenMinute, minute);

    Assert.assertEquals(
      actual, expected, "rate @ " + ((clock.millis() - START.toInstant().toEpochMilli()) / 1000)
    );
  }
}
