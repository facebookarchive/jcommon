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
package com.facebook.util;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

/**
 * Tests {@link TimeInterval}
 */
public class TestTimeInterval {
  // Constant offset time zones
  private static final DateTimeZone UTC = DateTimeZone.forTimeZone(
    TimeZone.getTimeZone(
      "GMT"
    ));
  private static final DateTimeZone EST = DateTimeZone.forTimeZone(
    TimeZone.getTimeZone(
      "GMT-5"
    ));
  private static final DateTimeZone PST = DateTimeZone.forTimeZone(
    TimeZone.getTimeZone(
      "GMT-8"
    ));
  private static final DateTimeZone IST = DateTimeZone.forTimeZone(
    TimeZone.getTimeZone(
      "GMT+5:30"
    ));
  // Daylight savings time zone
  private static final DateTimeZone PDT = DateTimeZone.forTimeZone(
    TimeZone.getTimeZone(
      "America/Los_Angeles"
    ));

  private static List<DateTimeZone> getTestTimeZones() {
    return Arrays.asList(UTC, EST, PST, IST, PDT);
  }

  @DataProvider(name = "durationParams")
  public Object[][] durationParams() throws Exception {
    // Use timezones with constant offsets for testing

    final int day = 1000 * 60 * 60 * 24;
    final int hour = 1000 * 60 * 60;
    final int minute = 1000 * 60;
    final int minute5 = 1000 * 60 * 5;
    final DateTime time = new DateTime(2011, 11, 3, 20, 21, 10, 14, PST);
    return new Object[][]{
      // day intervals
      {time, day, PST, new DateTime(2011, 11, 3, 0, 0, 0, 0, PST )},
      {time, day, EST, new DateTime( 2011, 11, 3, 0, 0, 0, 0, EST)},
      {time, day, UTC, new DateTime(2011, 11, 4, 0, 0, 0, 0, UTC )},
      {time, day, IST, new DateTime( 2011, 11, 4, 0, 0, 0, 0, IST)},
      // hour intervals
      {time, hour, PST, new DateTime(2011, 11, 3, 20, 0, 0, 0, PST )},
      {time, hour, EST, new DateTime( 2011, 11, 3, 23, 0, 0, 0, EST)},
      {time, hour, UTC, new DateTime(2011, 11, 4, 4, 0, 0, 0, UTC )},
      {time, hour, IST, new DateTime( 2011, 11, 4, 9, 0, 0, 0, IST)},
      // minute intervals
      {time, minute, PST, new DateTime(2011, 11, 3, 20, 21, 0, 0, PST )},
      {time, minute, EST, new DateTime( 2011, 11, 3, 23, 21, 0, 0, EST)},
      {time, minute, UTC, new DateTime(2011, 11, 4, 4, 21, 0, 0, UTC )},
      {time, minute, IST, new DateTime( 2011, 11, 4, 9, 51, 0, 0, IST)},
      // minute5 intervals
      {time, minute5, PST, new DateTime(2011, 11, 3, 20, 20, 0, 0, PST )},
      {time, minute5, EST, new DateTime( 2011, 11, 3, 23, 20, 0, 0, EST)},
      {time, minute5, UTC, new DateTime(2011, 11, 4, 4, 20, 0, 0, UTC )},
      {time, minute5, IST, new DateTime( 2011, 11, 4, 9, 50, 0, 0, IST)},
      // does not handle PDT correctly
      {new DateTime(1310579420000L, PDT), day, PDT, new DateTime(2011, 7, 13, 1, 0, 0, 0, PDT)},
      // handles INFINITE interval correctly
      {time, 0, PST, new DateTime(1970, 1, 1, 0, 0, 0, 0, PST)},
      // handles ZERO interval correctly
      {time, -1, PST, time}
    };
  }

  @Test(groups = "fast", dataProvider = "durationParams")
  public void testDurationStartOfInterval(
    DateTime eventTime,
    int intervalMillis,
    DateTimeZone tz,
    DateTime expectedValue) throws Exception {
    TimeInterval timeInterval = intervalMillis == 0
      ? TimeInterval.INFINITE
      : intervalMillis == -1 ? TimeInterval.ZERO : TimeInterval.withMillis(intervalMillis);
    Assert.assertEquals(
      timeInterval.getIntervalStart(eventTime.withZone(tz)),
      expectedValue
    );
    Assert.assertFalse(timeInterval.isPeriod());
    Assert.assertEquals(timeInterval.getLength(), intervalMillis);
    Assert.assertEquals(timeInterval.toApproxMillis(), intervalMillis);
    Assert.assertNull(timeInterval.getType());
  }

  @DataProvider(name = "intervalParams")
  public Object[][] intervalparams() {
    List<Object[]> params = new ArrayList<Object[]>();
    for (DateTimeZone timeZone: getTestTimeZones()) {
      DateTime time = new DateTime(2011, 8, 9, 14, 35, 12, 17, timeZone);
      params.addAll(Arrays.asList(new Object[][] {
        {time, TimeIntervalType.SECOND, 5, new DateTime(2011 ,8, 9, 14, 35, 10, 0, timeZone)},
        {time, TimeIntervalType.SECOND, 6, new DateTime(2011 ,8, 9, 14, 35, 12, 0, timeZone)},
        {time, TimeIntervalType.SECOND, 7, new DateTime(2011 ,8, 9, 14, 35, 7, 0, timeZone)},
        {time, TimeIntervalType.SECOND, 30, new DateTime(2011 ,8, 9, 14, 35, 0, 0, timeZone)},
        {time, TimeIntervalType.MINUTE, 6, new DateTime(2011 ,8, 9, 14, 30, 0, 0, timeZone)},
        {time, TimeIntervalType.HOUR, 1, new DateTime(2011 ,8, 9, 14, 0, 0, 0, timeZone)},
        {time, TimeIntervalType.HOUR, 6, new DateTime(2011 ,8, 9, 12, 0, 0, 0, timeZone)},
        {time, TimeIntervalType.DAY, 1, new DateTime(2011 ,8, 9, 0, 0, 0, 0, timeZone)},
        {time, TimeIntervalType.DAY, 7, new DateTime(2011 ,8, 8, 0, 0, 0, 0, timeZone)},
        // Note saturday is the start of week because the 2011-1-1 was a Saturday
        {time, TimeIntervalType.WEEK, 1, new DateTime(2011 ,8, 6, 0, 0, 0, 0, timeZone)},
        {time, TimeIntervalType.MONTH, 1, new DateTime(2011 ,8, 1, 0, 0, 0, 0, timeZone)},
        {time, TimeIntervalType.YEAR, 1, new DateTime(2011 ,1, 1, 0, 0, 0, 0, timeZone)},
        {time, TimeIntervalType.YEAR, 3, new DateTime(2009 ,1, 1, 0, 0, 0, 0, timeZone)},
      }));
    }
    return params.toArray(new Object[params.size()][]);
  }

  @Test(groups = "fast", dataProvider = "intervalParams")
  public void testPeriodStartOfInterval(
    DateTime eventTime,
    TimeIntervalType intervalType,
    int length,
    DateTime expectedValue) throws Exception {
    TimeInterval timeInterval = TimeInterval.withTypeAndLength(intervalType, length);
    Assert.assertEquals(timeInterval.getIntervalStart(eventTime), expectedValue);
    Assert.assertTrue(timeInterval.isPeriod());
    Assert.assertEquals(timeInterval.getLength(), length);
    Assert.assertEquals(timeInterval.getType(), intervalType);
  }

  @DataProvider(name = "intervalLengthFail")
  public Object[][] intervalLengthFail() {
    List<Object[]> params = new ArrayList<Object[]>();
    for (DateTimeZone timeZone: getTestTimeZones()) {
      params.addAll(Arrays.asList(new Object[][] {
        {TimeIntervalType.MILLIS, timeZone, 0},
        {TimeIntervalType.MILLIS, timeZone, 1000},
        {TimeIntervalType.SECOND, timeZone, 0},
        {TimeIntervalType.SECOND, timeZone, 60},
        {TimeIntervalType.MINUTE, timeZone, 0},
        {TimeIntervalType.MINUTE, timeZone, 60},
        {TimeIntervalType.HOUR, timeZone, 0},
        {TimeIntervalType.HOUR, timeZone, 24},
        {TimeIntervalType.DAY, timeZone, 0},
        {TimeIntervalType.DAY, timeZone, 32},
        {TimeIntervalType.WEEK, timeZone, 0},
        {TimeIntervalType.WEEK, timeZone, 54},
        {TimeIntervalType.MONTH, timeZone, 0},
        {TimeIntervalType.MONTH, timeZone, 13},
        {TimeIntervalType.YEAR, timeZone, 0},
      }));
    }
    return params.toArray(new Object[params.size()][]);
  }

  @Test(groups = "fast",
        dataProvider = "intervalLengthFail",
        expectedExceptions = {IllegalArgumentException.class},
        expectedExceptionsMessageRegExp = ".+is out of bounds for .+")
  public void testIntervalLengthOutOfBounds(
    TimeIntervalType intervalType,
    DateTimeZone timeZone,
    long length) {
    intervalType.validateValue(timeZone, length);
  }

  @DataProvider(name = "intervalsValid")
  public Object[][] intervalsValid() {
    List<Object[]> params = new ArrayList<Object[]>();
    // millis in a second
    final long oneSec = 1000L;
    final long oneDay = 24 * 60 * 60 * oneSec;
    final long oneYear = 365 * oneDay + (5 * 3600 + 2952) * oneSec;
    for (DateTimeZone timeZone: getTestTimeZones()) {
      params.addAll(Arrays.asList(new Object[][] {
        {TimeIntervalType.MILLIS, timeZone, 1, 1L},
        {TimeIntervalType.MILLIS, timeZone, 999, 999L},
        {TimeIntervalType.SECOND, timeZone, 1, oneSec},
        {TimeIntervalType.SECOND, timeZone, 59, 59 * oneSec},
        {TimeIntervalType.MINUTE, timeZone, 1, 60 * oneSec},
        {TimeIntervalType.MINUTE, timeZone, 59, 59 * 60 * oneSec},
        {TimeIntervalType.HOUR, timeZone, 1, 60 * 60 * oneSec},
        {TimeIntervalType.HOUR, timeZone, 23, 23 * 60 * 60 * oneSec},
        {TimeIntervalType.DAY, timeZone, 1, oneDay},
        {TimeIntervalType.DAY, timeZone, 31, 31 * oneDay},
        {TimeIntervalType.WEEK, timeZone, 1, 7 * oneDay},
        {TimeIntervalType.WEEK, timeZone, 53, 53 * 7 * oneDay},
        // Gives you a non intuitive value, Beware!
        {TimeIntervalType.MONTH, timeZone, 1, 30 * oneDay + 37746 * oneSec},
        // Gives you a non intuitive value, Beware!
        {TimeIntervalType.MONTH, timeZone, 12, oneYear},
        // Gives you a non intuitive value, Beware!
        {TimeIntervalType.YEAR, timeZone, 1, oneYear},
      }));
    }
    return params.toArray(new Object[params.size()][]);
  }

  @Test(groups = "fast", dataProvider = "intervalsValid")
  public void testValidIntervalLength(
    TimeIntervalType intervalType,
    DateTimeZone timeZone,
    int length,
    long millisValue
  ) {
    intervalType.validateValue(timeZone, length);
    Assert.assertEquals(
      TimeInterval.withTypeAndLength(intervalType, length).toApproxMillis(),
      millisValue);
  }

  @DataProvider(name = "plusMinus")
  public Object[][] plusMinus() {
    // 2011-3-13T00:03:00 is DST start
    final DateTime testTime = new DateTime(2011, 3, 13, 0, 29, 10, 101, PDT);
    return new Object[][] {
      {testTime, TimeInterval.withTypeAndLength(TimeIntervalType.MILLIS, 1), new DateTime(2011, 3, 13, 0, 29, 10, 102, PDT), 1},
      {testTime, TimeInterval.withTypeAndLength(TimeIntervalType.SECOND, 1), new DateTime(2011, 3, 13, 0, 29, 11, 101, PDT), 1},
      {testTime, TimeInterval.withTypeAndLength(TimeIntervalType.MINUTE, 5), new DateTime(2011, 3, 13, 0, 34, 10, 101, PDT), 1},
      {testTime, TimeInterval.withTypeAndLength(TimeIntervalType.MINUTE, 121), new DateTime(2011, 3, 13, 3, 30, 10, 101, PDT), 1},
      {testTime, TimeInterval.withTypeAndLength(TimeIntervalType.HOUR, 2), new DateTime(2011, 3, 13, 3, 29, 10, 101, PDT), 1},
      {testTime, TimeInterval.withTypeAndLength(TimeIntervalType.DAY, 1), new DateTime(2011, 3, 14, 0, 29, 10, 101, PDT), 1},
      {testTime, TimeInterval.withTypeAndLength(TimeIntervalType.MONTH, 3), new DateTime(2011, 6, 13, 0, 29, 10, 101, PDT), 1},
      {testTime, TimeInterval.withTypeAndLength(TimeIntervalType.WEEK, 7), new DateTime(2011, 5, 1, 0, 29, 10, 101, PDT), 1},
      // Tests out leap years too
      {testTime, TimeInterval.withTypeAndLength(TimeIntervalType.YEAR, 5), new DateTime(2016, 3, 13, 0, 29, 10, 101, PDT), 1},
      // Test multiple > 1
      {testTime, TimeInterval.withTypeAndLength(TimeIntervalType.MINUTE, 60), new DateTime(2011, 3, 13, 3, 29, 10, 101, PDT), 2},
      // Test exact instant of transition
      {new DateTime(2011, 3, 13, 1, 59, 59, 999, PDT), TimeInterval.withTypeAndLength(TimeIntervalType.MILLIS, 1), new DateTime(2011, 3, 13, 3, 0, 0, 0, PDT), 1},
      // Test duration based interval
      {new DateTime(2011, 3, 13, 1, 59, 59, 999, PDT), TimeInterval.withMillis(1), new DateTime(2011, 3, 13, 3, 0, 0, 0, PDT), 1},
      {testTime, TimeInterval.withMillis(24 * 60 * 60 * 1000), new DateTime(2011, 3, 14, 1, 29, 10, 101, PDT), 1},
      {testTime, TimeInterval.ZERO, testTime, 1}
    };
  }

  @Test(groups = "fast", dataProvider = "plusMinus")
  public void testIntervalPlusMinus(
    DateTime before,
    TimeInterval interval,
    DateTime after,
    int multiple) {
    Assert.assertEquals(interval.plus(before, multiple), after);
    Assert.assertEquals(interval.minus(after, multiple), before);
  }

  @Test(groups = "fast")
  public void testEqualsAndHashcode() {
    TimeInterval []intervals = {
      TimeInterval.ZERO,
      TimeInterval.INFINITE,
      TimeInterval.withMillis(1),
      TimeInterval.withMillis(2),
      TimeInterval.withTypeAndLength(TimeIntervalType.DAY, 1),
      TimeInterval.withTypeAndLength(TimeIntervalType.DAY, 2),
      TimeInterval.withTypeAndLength(TimeIntervalType.HOUR, 1),
    };
    for (int i = 0; i < intervals.length; i++) {
      TimeInterval interval1 = intervals[i];
      for (int j = 0; j < intervals.length; j++) {
        TimeInterval interval2 = intervals[j];
        if (i == j) {
          Assert.assertEquals(interval1, interval2);
          Assert.assertEquals(interval1.hashCode(), interval2.hashCode());
        } else {
          Assert.assertFalse(interval1.equals(interval2));
          // TimeInterval.ZERO and TimeInterval.INFINITE happens to have the
          // same hash code:
          // 11111111111111111111111111111111 XOR 11111111111111111111111111111111 = 0
          // 00000000000000000000000000000000 XOR 00000000000000000000000000000000 = 0
          // Since the hash code function maps a long (length) to an int, it's bound
          // to have *some* collisions, and according to Anshul the likelyhood of these
          // being used as keys is low. Also equals() could tell the difference of
          // TimeInterval.ZERO and TimeInterval.INFINITE, it's not a correctness issue.
          // No big deal but a little special handling is needed in unit testing. :-)
          Assert.assertTrue(interval1.hashCode() != interval2.hashCode() ||
          interval1 == TimeInterval.ZERO && interval2 == TimeInterval.INFINITE ||
          interval1 == TimeInterval.INFINITE && interval2 == TimeInterval.ZERO);
        }
      }
    }
    Assert.assertFalse(TimeInterval.INFINITE.equals(null));
    Assert.assertFalse(TimeInterval.INFINITE.equals(new Object()));
    Assert.assertFalse(TimeInterval.ZERO.equals(null));
    Assert.assertFalse(TimeInterval.ZERO.equals(new Object()));
    Assert.assertFalse(TimeInterval.ZERO.equals(TimeInterval.INFINITE));
  }

  @Test(groups = "fast",
        expectedExceptions = {IllegalArgumentException.class},
        expectedExceptionsMessageRegExp = "length cannot be less than one: 0")
  public void testInvalidMillis() {
    TimeInterval.withMillis(0);
  }

  @Test(groups = "fast",
        expectedExceptions = {IllegalArgumentException.class},
        expectedExceptionsMessageRegExp = "type cannot be null")
  public void testNullType() {
    TimeInterval.withTypeAndLength(null, 1);
  }

  @Test(groups = "fast",
        expectedExceptions = {IllegalArgumentException.class},
        expectedExceptionsMessageRegExp = "length cannot be less than one: 0")
  public void testInvalidLength() {
    TimeInterval.withTypeAndLength(TimeIntervalType.DAY, 0);
  }

  @Test(groups = "fast",
        expectedExceptions = {IllegalArgumentException.class},
        expectedExceptionsMessageRegExp = "Multiple cannot be less that 0 : -1")
  public void testInvalidMultiple() {
    TimeInterval.withMillis(10).plus(new DateTime(1000), -1);
  }

  @Test(groups = "fast", expectedExceptions = {IllegalStateException.class})
  public void testInfinitePlus() {
    TimeInterval.INFINITE.plus(new DateTime(1000), 1);
  }

  @Test(groups = "fast", expectedExceptions = {IllegalStateException.class})
  public void testInfiniteMinus() {
    DateTime testTimePDT = new DateTime(2011, 3, 13, 0, 29, 10, 101, PDT);
    TimeInterval.INFINITE.minus(testTimePDT, 1);
  }
}
