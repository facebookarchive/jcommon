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

import com.google.common.collect.ImmutableMap;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.chrono.ISOChronology;

import java.util.Map;

import com.facebook.logging.Logger;
import com.facebook.logging.LoggerImpl;

public class TimeUtil {
  private static final Logger LOG = LoggerImpl.getLogger(TimeUtil.class);

  // DateTimeZone.forID() and ISOChronology.getInstance() are very expensive,
  // so we statically pre-compute a fast lookup table.
  private static final Map<String, DateTimeZone> TIME_ZONE_MAP;
  private static final Map<String, ISOChronology> CHRONOLOGY_MAP;

  static {
    ImmutableMap.Builder<String, DateTimeZone> timeZoneBuilder =
      new ImmutableMap.Builder<String, DateTimeZone>();
    ImmutableMap.Builder<String, ISOChronology> chronologyBuilder =
      new ImmutableMap.Builder<String, ISOChronology>();

    for (Object id : DateTimeZone.getAvailableIDs()) {
      String tz = (String) id;
      DateTimeZone timeZone = DateTimeZone.forID(tz);
      timeZoneBuilder.put(tz, timeZone);
      chronologyBuilder.put(tz, ISOChronology.getInstance(timeZone));
    }
    TIME_ZONE_MAP = timeZoneBuilder.build();
    CHRONOLOGY_MAP = chronologyBuilder.build();
  }

  // utility method to log how long a chunk of code takes to run
  public static <E extends Throwable> void logElapsedTime(
    String tag, ExtRunnable<E> task
  ) throws E {
    long start = DateTimeUtils.currentTimeMillis();
    boolean success = false;

    try {
      task.run();
      success = true;
    } finally {
      LOG.info(
        "%s (%s) elapsed time(ms): %d",
        tag,
        success,
        DateTimeUtils.currentTimeMillis() - start
      );
    }
  }

  // utility method to log how long a chunk of code takes to run
  public static <V, E extends Throwable> V logElapsedTime(
    String tag, ExtCallable<V, E> task
  ) throws E {
    long start = DateTimeUtils.currentTimeMillis();
    boolean success = false;

    try {
      V value = task.call();
      success = true;

      return value;
    } finally {
      LOG.info(
        "%s (%s) elapsed time(ms): %d",
        tag,
        success,
        DateTimeUtils.currentTimeMillis() - start
      );
    }
  }

  public static DateTimeZone getDateTimeZone(String dateTimeZoneStr) {
      if ((dateTimeZoneStr == null) || dateTimeZoneStr.isEmpty()) {
        return DateTimeZone.UTC;
      }
      return TIME_ZONE_MAP.get(dateTimeZoneStr);
    }

  public static ISOChronology getChronology(String dateTimeZoneStr) {
    if ((dateTimeZoneStr == null) || dateTimeZoneStr.isEmpty()) {
      dateTimeZoneStr = DateTimeZone.UTC.getID();
    }
    return CHRONOLOGY_MAP.get(dateTimeZoneStr);
  }

  /**
   * these methods affect only code that relies on DateTimeUtils.currentTimeMillis()
   *
   * NOTE: manipulation of {@link DateTimeUtils.currentTimeMillis()} is not thread safe
   * to begin with, so neither is this
   */
  public static void setNow(DateTime now) {
    DateTimeUtils.setCurrentMillisFixed(now.getMillis());
  }

  public static void advanceNow(Duration duration) {
    long now = DateTimeUtils.currentTimeMillis();

    DateTimeUtils.setCurrentMillisFixed(now + duration.getMillis());
  }
}
