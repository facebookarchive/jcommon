/*
 * Copyright (C) 2016 Facebook, Inc.
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
import org.testng.annotations.Test;

import static com.facebook.util.TimeIntervalType.DAY;
import static com.facebook.util.TimeIntervalType.HOUR;
import static com.facebook.util.TimeIntervalType.MILLIS;
import static com.facebook.util.TimeIntervalType.MINUTE;
import static com.facebook.util.TimeIntervalType.MONTH;
import static com.facebook.util.TimeIntervalType.SECOND;
import static com.facebook.util.TimeIntervalType.WEEK;
import static com.facebook.util.TimeIntervalType.YEAR;
import static org.joda.time.DateTimeZone.UTC;

/**
 * Mostly conformance testing: these aren't testing how things <em>should</em> work but rather how
 * they <em>do</em> work (since there's a lot of code that relies on the existing behavior).
 */
public class TestTimeIntervalType {
  private static final DateTimeZone PDT = DateTimeZone.forID("America/Los_Angeles");

  @Test
  public void testMillisIntervalStart() throws Exception {
    assertStart("2016-04-13T11:26:47.856-07:00", PDT, MILLIS, 1, "2016-04-13T11:26:47.856-07:00");
    assertStart("2016-04-13T11:26:47.856-07:00", PDT, MILLIS, 100, "2016-04-13T11:26:47.800-07:00");
    assertStart("2016-04-13T11:26:48.356-07:00", PDT, MILLIS, 1, "2016-04-13T11:26:48.356-07:00");
    assertStart("2016-04-13T11:26:48.356-07:00", PDT, MILLIS, 100, "2016-04-13T11:26:48.300-07:00");
  }

  @Test
  public void testSecondsIntervalStart() throws Exception {
    assertStart("2016-04-13T11:25:51.302-07:00", PDT, SECOND, 1, "2016-04-13T11:25:51.000-07:00");
    assertStart("2016-04-13T11:25:51.302-07:00", PDT, SECOND, 10, "2016-04-13T11:25:50.000-07:00");
    assertStart("2016-04-13T11:25:51.302-07:00", PDT, SECOND, 30, "2016-04-13T11:25:30.000-07:00");
    assertStart("2016-04-13T11:26:11.302-07:00", PDT, SECOND, 1, "2016-04-13T11:26:11.000-07:00");
    assertStart("2016-04-13T11:26:11.302-07:00", PDT, SECOND, 10, "2016-04-13T11:26:10.000-07:00");
    assertStart("2016-04-13T11:26:11.302-07:00", PDT, SECOND, 30, "2016-04-13T11:26:00.000-07:00");
  }

  @Test
  public void testMinuteIntervalStart() throws Exception {
    assertStart("2016-04-13T11:20:29.330-07:00", PDT, MINUTE, 1, "2016-04-13T11:20:00.000-07:00");
    assertStart("2016-04-13T11:20:29.330-07:00", PDT, MINUTE, 5, "2016-04-13T11:20:00.000-07:00");
    assertStart("2016-04-13T11:20:29.330-07:00", PDT, MINUTE, 15, "2016-04-13T11:15:00.000-07:00");
    assertStart("2016-04-13T11:20:29.330-07:00", PDT, MINUTE, 30, "2016-04-13T11:00:00.000-07:00");
    assertStart("2016-04-13T11:40:29.330-07:00", PDT, MINUTE, 1, "2016-04-13T11:40:00.000-07:00");
    assertStart("2016-04-13T11:40:29.330-07:00", PDT, MINUTE, 5, "2016-04-13T11:40:00.000-07:00");
    assertStart("2016-04-13T11:40:29.330-07:00", PDT, MINUTE, 15, "2016-04-13T11:30:00.000-07:00");
    assertStart("2016-04-13T11:40:29.330-07:00", PDT, MINUTE, 30, "2016-04-13T11:30:00.000-07:00");
  }

  @Test
  public void testDayIntervalStartUTC() throws Exception {
    assertStart("2016-01-13T00:52:38.337Z", UTC, DAY, 1, "2016-01-13T00:00:00.000Z");
    assertStart("2016-02-13T02:52:38.337Z", UTC, DAY, 2, "2016-02-13T00:00:00.000Z");
    assertStart("2016-03-13T04:52:38.337Z", UTC, DAY, 3, "2016-03-13T00:00:00.000Z");
    assertStart("2016-04-13T06:52:38.337Z", UTC, DAY, 5, "2016-04-11T00:00:00.000Z");
    assertStart("2016-05-13T08:52:38.337Z", UTC, DAY, 7, "2016-05-08T00:00:00.000Z");
    assertStart("2016-06-13T10:52:38.337Z", UTC, DAY, 14, "2016-06-01T00:00:00.000Z");
    assertStart("2016-07-13T12:52:38.337Z", UTC, DAY, 30, "2016-07-01T00:00:00.000Z");
    assertStart("2016-08-13T14:52:38.337Z", UTC, DAY, 1, "2016-08-13T00:00:00.000Z");
    assertStart("2016-09-13T16:52:38.337Z", UTC, DAY, 2, "2016-09-13T00:00:00.000Z");
    assertStart("2016-10-13T18:52:38.337Z", UTC, DAY, 3, "2016-10-13T00:00:00.000Z");
    assertStart("2016-11-13T20:52:38.337Z", UTC, DAY, 5, "2016-11-11T00:00:00.000Z");
    assertStart("2016-12-13T22:52:38.337Z", UTC, DAY, 7, "2016-12-08T00:00:00.000Z");
  }

  @Test
  public void testDayIntervalStartPDT() throws Exception {
    assertStart("2016-01-13T00:51:03.772-08:00", PDT, DAY, 1, "2016-01-13T00:00:00.000-08:00");
    assertStart("2016-02-13T02:51:03.772-08:00", PDT, DAY, 2, "2016-02-13T00:00:00.000-08:00");
    assertStart("2016-03-13T04:51:03.772-07:00", PDT, DAY, 3, "2016-03-13T00:00:00.000-08:00");
    assertStart("2016-04-13T06:51:03.772-07:00", PDT, DAY, 5, "2016-04-11T00:00:00.000-07:00");
    assertStart("2016-05-13T08:51:03.772-07:00", PDT, DAY, 7, "2016-05-08T00:00:00.000-07:00");
    assertStart("2016-06-13T10:51:03.772-07:00", PDT, DAY, 14, "2016-06-01T00:00:00.000-07:00");
    assertStart("2016-07-13T12:51:03.772-07:00", PDT, DAY, 30, "2016-07-01T00:00:00.000-07:00");
    assertStart("2016-08-13T14:51:03.772-07:00", PDT, DAY, 1, "2016-08-13T00:00:00.000-07:00");
    assertStart("2016-09-13T16:51:03.772-07:00", PDT, DAY, 2, "2016-09-13T00:00:00.000-07:00");
    assertStart("2016-10-13T18:51:03.772-07:00", PDT, DAY, 3, "2016-10-13T00:00:00.000-07:00");
    assertStart("2016-11-13T20:51:03.772-08:00", PDT, DAY, 5, "2016-11-11T00:00:00.000-08:00");
    assertStart("2016-12-13T22:51:03.772-08:00", PDT, DAY, 7, "2016-12-08T00:00:00.000-08:00");
  }

  @Test
  public void testHourIntervalStartUTC() throws Exception {
    assertStart("2016-01-02T00:00:00.000Z", UTC, HOUR, 1, "2016-01-02T00:00:00.000Z");
    assertStart("2016-01-02T02:00:00.000Z", UTC, HOUR, 3, "2016-01-02T00:00:00.000Z");
    assertStart("2016-01-02T04:00:00.000Z", UTC, HOUR, 7, "2016-01-02T00:00:00.000Z");
    assertStart("2016-01-02T06:00:00.000Z", UTC, HOUR, 13, "2016-01-02T00:00:00.000Z");
    assertStart("2016-01-02T08:00:00.000Z", UTC, HOUR, 21, "2016-01-02T00:00:00.000Z");
    assertStart("2016-01-02T10:00:00.000Z", UTC, HOUR, 1, "2016-01-02T10:00:00.000Z");
    assertStart("2016-01-02T12:00:00.000Z", UTC, HOUR, 3, "2016-01-02T12:00:00.000Z");
    assertStart("2016-01-02T14:00:00.000Z", UTC, HOUR, 7, "2016-01-02T14:00:00.000Z");
    assertStart("2016-01-02T16:00:00.000Z", UTC, HOUR, 13, "2016-01-02T13:00:00.000Z");
    assertStart("2016-01-02T18:00:00.000Z", UTC, HOUR, 21, "2016-01-02T00:00:00.000Z");
    assertStart("2016-01-02T20:00:00.000Z", UTC, HOUR, 1, "2016-01-02T20:00:00.000Z");
    assertStart("2016-01-02T22:00:00.000Z", UTC, HOUR, 3, "2016-01-02T21:00:00.000Z");
  }

  @Test
  public void testHourIntervalStartPDT() throws Exception {
    assertStart("2016-01-02T00:00:00.000-08:00", PDT, HOUR, 1, "2016-01-02T00:00:00.000-08:00");
    assertStart("2016-01-02T02:00:00.000-08:00", PDT, HOUR, 3, "2016-01-02T00:00:00.000-08:00");
    assertStart("2016-01-02T04:00:00.000-08:00", PDT, HOUR, 7, "2016-01-02T00:00:00.000-08:00");
    assertStart("2016-01-02T06:00:00.000-08:00", PDT, HOUR, 13, "2016-01-02T00:00:00.000-08:00");
    assertStart("2016-01-02T08:00:00.000-08:00", PDT, HOUR, 21, "2016-01-02T00:00:00.000-08:00");
    assertStart("2016-01-02T10:00:00.000-08:00", PDT, HOUR, 1, "2016-01-02T10:00:00.000-08:00");
    assertStart("2016-01-02T12:00:00.000-08:00", PDT, HOUR, 3, "2016-01-02T12:00:00.000-08:00");
    assertStart("2016-01-02T14:00:00.000-08:00", PDT, HOUR, 7, "2016-01-02T14:00:00.000-08:00");
    assertStart("2016-01-02T16:00:00.000-08:00", PDT, HOUR, 13, "2016-01-02T13:00:00.000-08:00");
    assertStart("2016-01-02T18:00:00.000-08:00", PDT, HOUR, 21, "2016-01-02T00:00:00.000-08:00");
    assertStart("2016-01-02T20:00:00.000-08:00", PDT, HOUR, 1, "2016-01-02T20:00:00.000-08:00");
    assertStart("2016-01-02T22:00:00.000-08:00", PDT, HOUR, 3, "2016-01-02T21:00:00.000-08:00");
  }

  @Test
  public void testWeekIntervalStartUTC() throws Exception {
    assertStart("2016-04-05T00:00:00.000Z", UTC, WEEK, 1, "2016-04-01T00:00:00.000Z");
    assertStart("2016-04-05T00:00:00.000Z", UTC, WEEK, 2, "2016-03-25T00:00:00.000Z");
    assertStart("2016-04-05T00:00:00.000Z", UTC, WEEK, 3, "2016-03-25T00:00:00.000Z");
    assertStart("2016-04-05T00:00:00.000Z", UTC, WEEK, 11, "2016-03-18T00:00:00.000Z");
    assertStart("2016-04-05T00:00:00.000Z", UTC, WEEK, 26, "2016-01-01T00:00:00.000Z");
    assertStart("2016-04-05T00:00:00.000Z", UTC, WEEK, 50, "2016-01-01T00:00:00.000Z");
  }

  @Test
  public void testWeekIntervalStartPDT() throws Exception {
    assertStart("2016-04-05T00:00:00.000-07:00", PDT, WEEK, 1, "2016-04-01T00:00:00.000-07:00");
    assertStart("2016-04-05T00:00:00.000-07:00", PDT, WEEK, 2, "2016-03-25T00:00:00.000-07:00");
    assertStart("2016-04-05T00:00:00.000-07:00", PDT, WEEK, 3, "2016-03-25T00:00:00.000-07:00");
    assertStart("2016-04-05T00:00:00.000-07:00", PDT, WEEK, 11, "2016-03-18T00:00:00.000-07:00");
    assertStart("2016-04-05T00:00:00.000-07:00", PDT, WEEK, 26, "2016-01-01T00:00:00.000-08:00");
    assertStart("2016-04-05T00:00:00.000-07:00", PDT, WEEK, 50, "2016-01-01T00:00:00.000-08:00");
  }

  @Test
  public void testMonthIntervalStartUTC() throws Exception {
    assertStart("2016-04-05T00:00:00.000Z", UTC, MONTH, 1, "2016-04-01T00:00:00.000Z");
    assertStart("2016-04-05T00:00:00.000Z", UTC, MONTH, 2, "2016-03-01T00:00:00.000Z");
    assertStart("2016-04-05T00:00:00.000Z", UTC, MONTH, 3, "2016-04-01T00:00:00.000Z");
    assertStart("2016-04-05T00:00:00.000Z", UTC, MONTH, 7, "2016-01-01T00:00:00.000Z");
    assertStart("2016-04-05T00:00:00.000Z", UTC, MONTH, 12, "2016-01-01T00:00:00.000Z");
  }

  @Test
  public void testMonthIntervalStartPDT() throws Exception {
    assertStart("2016-04-05T00:00:00.000-07:00", PDT, MONTH, 1, "2016-04-01T00:00:00.000-07:00");
    assertStart("2016-04-05T00:00:00.000-07:00", PDT, MONTH, 2, "2016-03-01T00:00:00.000-08:00");
    assertStart("2016-04-05T00:00:00.000-07:00", PDT, MONTH, 3, "2016-04-01T00:00:00.000-07:00");
    assertStart("2016-04-05T00:00:00.000-07:00", PDT, MONTH, 7, "2016-01-01T00:00:00.000-08:00");
    assertStart("2016-04-05T00:00:00.000-07:00", PDT, MONTH, 12, "2016-01-01T00:00:00.000-08:00");
  }

  @Test
  public void testYearIntervalStartUTC() throws Exception {
    assertStart("2016-04-05T00:00:00.000Z", UTC, YEAR, 1, "2016-01-01T00:00:00.000Z");
    assertStart("2016-04-05T00:00:00.000Z", UTC, YEAR, 2, "2016-01-01T00:00:00.000Z");
    assertStart("2016-04-05T00:00:00.000Z", UTC, YEAR, 3, "2015-01-01T00:00:00.000Z");
    assertStart("2016-04-05T00:00:00.000Z", UTC, YEAR, 4, "2016-01-01T00:00:00.000Z");
    assertStart("2016-04-05T00:00:00.000Z", UTC, YEAR, 5, "2015-01-01T00:00:00.000Z");
    assertStart("2016-04-05T00:00:00.000Z", UTC, YEAR, 6, "2012-01-01T00:00:00.000Z");
    assertStart("2016-04-05T00:00:00.000Z", UTC, YEAR, 7, "2014-01-01T00:00:00.000Z");
    assertStart("2016-04-05T00:00:00.000Z", UTC, YEAR, 8, "2016-01-01T00:00:00.000Z");
    assertStart("2016-04-05T00:00:00.000Z", UTC, YEAR, 9, "2009-01-01T00:00:00.000Z");
    assertStart("2016-04-05T00:00:00.000Z", UTC, YEAR, 10, "2010-01-01T00:00:00.000Z");
  }

  @Test
  public void testYearIntervalStartPDT() throws Exception {
    assertStart("2016-04-05T00:00:00.000-07:00", PDT, YEAR, 1, "2016-01-01T00:00:00.000-08:00");
    assertStart("2016-04-05T00:00:00.000-07:00", PDT, YEAR, 2, "2016-01-01T00:00:00.000-08:00");
    assertStart("2016-04-05T00:00:00.000-07:00", PDT, YEAR, 3, "2015-01-01T00:00:00.000-08:00");
    assertStart("2016-04-05T00:00:00.000-07:00", PDT, YEAR, 4, "2016-01-01T00:00:00.000-08:00");
    assertStart("2016-04-05T00:00:00.000-07:00", PDT, YEAR, 5, "2015-01-01T00:00:00.000-08:00");
    assertStart("2016-04-05T00:00:00.000-07:00", PDT, YEAR, 6, "2012-01-01T00:00:00.000-08:00");
    assertStart("2016-04-05T00:00:00.000-07:00", PDT, YEAR, 7, "2014-01-01T00:00:00.000-08:00");
    assertStart("2016-04-05T00:00:00.000-07:00", PDT, YEAR, 8, "2016-01-01T00:00:00.000-08:00");
    assertStart("2016-04-05T00:00:00.000-07:00", PDT, YEAR, 9, "2009-01-01T00:00:00.000-08:00");
    assertStart("2016-04-05T00:00:00.000-07:00", PDT, YEAR, 10, "2010-01-01T00:00:00.000-08:00");
  }

  @Test
  public void testHourGetTimeIntervalStartGap() throws Exception {
    DateTimeZone timeZone = DateTimeZone.forID("America/Havana");

    // Daylight saving causes a gap at midnight: time jumps from midnight to 1am on the 13th.
    // This test checks that hourly code can correctly handle 00:00 not being a valid time.
    assertStart("2016-03-13T15:09:26.535-0400", timeZone, HOUR, 1, "2016-03-13T15:00:00-0400");
  }

  @Test
  public void testDayGetTimeIntervalStartGap() throws Exception {
    DateTimeZone timeZone = DateTimeZone.forID("Asia/Amman");

    // Daylight saving causes a gap at midnight: time jumps from midnight to 1am on the 1st.
    // This test checks that daily code can correctly handle 04-01T00:00 not being a valid time.
    assertStart("2016-04-12T15:09:26.535+0200", timeZone, DAY, 1, "2016-04-12T01:00:00.000+03:00");
  }

  private static void assertStart(
    String date, DateTimeZone timeZone, TimeIntervalType type, int length, String expected
  ) {
    DateTime actual = type.getTimeIntervalStart(new DateTime(date, timeZone), length);

    Assert.assertEquals(actual, new DateTime(expected, timeZone), length + " " + type);
  }
}
