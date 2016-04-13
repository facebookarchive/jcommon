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
import org.joda.time.DateTimeField;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeZone;
import org.joda.time.DurationField;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.field.FieldUtils;

import static org.joda.time.DateTimeFieldType.centuryOfEra;
import static org.joda.time.DateTimeFieldType.dayOfMonth;
import static org.joda.time.DateTimeFieldType.hourOfDay;
import static org.joda.time.DateTimeFieldType.millisOfSecond;
import static org.joda.time.DateTimeFieldType.minuteOfHour;
import static org.joda.time.DateTimeFieldType.monthOfYear;
import static org.joda.time.DateTimeFieldType.secondOfMinute;
import static org.joda.time.DateTimeFieldType.weekOfWeekyear;
import static org.joda.time.DateTimeFieldType.yearOfCentury;

/**
 * Represents a time interval type when specifying a time period. Instances of
 * this class provide means to calculate the start instant of the interval
 * containing a given time instant.
 *
 * <p>
 * Each interval type computes the time interval assuming that they start
 * from it's minimum value. For example, MINUTE represents the time interval
 * starting from the 0th minute in the containing hour. So given a time instant
 * 2011-10-09T13:11:49, and a time interval type of MINUTE and length 3, the
 * start instant of the time interval containing that instant will be
 * 2011-10-09T13:09:00.
 * </p>
 */
public enum TimeIntervalType {
  MILLIS(PeriodType.millis(), millisOfSecond(), secondOfMinute()) {
    @Override
    public Period toPeriod(int length) {
      return Period.millis(length);
    }
  },
  SECOND(PeriodType.seconds(), secondOfMinute(), minuteOfHour()) {
    @Override
    public Period toPeriod(int length) {
      return Period.seconds(length);
    }
  },
  MINUTE(PeriodType.minutes(), minuteOfHour(), hourOfDay()) {
    @Override
    public Period toPeriod(int length) {
      return Period.minutes(length);
    }
  },
  HOUR(PeriodType.hours(), hourOfDay(), dayOfMonth()) {
    @Override
    public Period toPeriod(int length) {
      return Period.hours(length);
    }
  },
  DAY(PeriodType.days(), dayOfMonth(), monthOfYear()) {
    @Override
    public Period toPeriod(int length) {
      return Period.days(length);
    }
  },
  /**
   * Week interval is currently non-intuitive. It assumes that the weeks
   * start on the same day as the first day of the year. Don't use weeks
   * as a time interval type, until we can have the weeks starting on the
   * correct day (Sunday/Monday) per the locale.
   */
  WEEK(PeriodType.weeks(), weekOfWeekyear(), yearOfCentury()) {
    @Override
    public Period toPeriod(int length) {
      return Period.weeks(length);
    }
  },
  MONTH(PeriodType.months(), monthOfYear(), yearOfCentury()) {
    @Override
    public Period toPeriod(int length) {
      return Period.months(length);
    }
  },
  YEAR(PeriodType.years(), yearOfCentury(), centuryOfEra()) {
    @Override
    public Period toPeriod(int length) {
      return Period.years(length);
    }
  };

  private final PeriodType periodType;
  private final DateTimeFieldType fieldType;
  private final DateTimeFieldType truncateFieldType;

  TimeIntervalType(
    PeriodType periodType, DateTimeFieldType fieldType, DateTimeFieldType truncateFieldType
  ) {
    this.periodType = periodType;
    this.fieldType = fieldType;
    this.truncateFieldType = truncateFieldType;
  }

  /**
   * Returns a period representing this interval type.
   *
   * @param length the multiple of the base period unit
   * @return the period value
   */
  public abstract Period toPeriod(int length);

  /**
   * Validates that the specified interval value is valid for this
   * interval type in the supplied time zone.
   *
   * @param timeZone the time zone
   * @param intervalLength the interval length
   */
  public void validateValue(DateTimeZone timeZone, long intervalLength) {
    final DateTimeField field = fieldType.getField(TimeUtil.getChronology(timeZone.getID()));
    if (intervalLength < 1
      || intervalLength > field.getMaximumValue()) {
      throw new IllegalArgumentException(
        "Supplied value " + intervalLength + " is out of bounds for " + name()
      );
    }
  }


  /**
   * Gets the start instant given the event instant, interval length
   * and the time zone for this interval type.
   *
   * @param instant the event time instant.
   * @param length the interval length
   *
   * @return the start instant of the interval of given length that contains
   * the supplied time instant in the supplied time zone
   */
  public DateTime getTimeIntervalStart(DateTime instant, long length) {
    validateValue(instant.getZone(), length);

    // Reset all the fields
    DateTime periodStart = instant.property(truncateFieldType)
      .roundFloorCopy();
    // figure out the which time interval does the instant lie in
    Period period = new Period(periodStart, instant, periodType);
    DurationField durationField = fieldType.getField(instant.getChronology()).getDurationField();
    int diff = period.get(durationField.getType());
    long startDelta = (diff / length) * length;

    return periodStart.withFieldAdded(durationField.getType(), FieldUtils.safeToInt(startDelta));
  }

  /**
   * Returns the number of milliseconds per unit of this interval type.
   *
   * @return the number of milliseconds per unit of this interval type.
   */
  public long toDurationMillis() {
    return fieldType.getDurationType().getField(
      // Assume that durations are always in UTC
      ISOChronology.getInstance(DateTimeZone.UTC)).getUnitMillis();
  }
}
