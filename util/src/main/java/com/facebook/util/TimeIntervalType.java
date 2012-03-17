package com.facebook.util;

import org.joda.time.DateTime;
import org.joda.time.DateTimeField;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.DurationField;
import org.joda.time.Period;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.field.FieldUtils;

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
  MILLIS(DateTimeFieldType.millisOfSecond(), null) {
    @Override
    public Period toPeriod(int length) {
      return Period.millis(length);
    }
  },
  SECOND(DateTimeFieldType.secondOfMinute(), MILLIS) {
    @Override
    public Period toPeriod(int length) {
      return Period.seconds(length);
    }
  },
  MINUTE(DateTimeFieldType.minuteOfHour(), SECOND) {
    @Override
    public Period toPeriod(int length) {
      return Period.minutes(length);
    }
  },
  HOUR(DateTimeFieldType.hourOfDay(), MINUTE) {
    @Override
    public Period toPeriod(int length) {
      return Period.hours(length);
    }
  },
  DAY(DateTimeFieldType.dayOfMonth(), HOUR) {
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
  WEEK(DateTimeFieldType.weekOfWeekyear(), DAY) {
    @Override
    public Period toPeriod(int length) {
      return Period.weeks(length);
    }
  },
  MONTH(DateTimeFieldType.monthOfYear(), DAY) {
    @Override
    public Period toPeriod(int length) {
      return Period.months(length);
    }
  },
  YEAR(DateTimeFieldType.yearOfCentury(), MONTH) {
    @Override
    public Period toPeriod(int length) {
      return Period.years(length);
    }
  };

  private final DateTimeFieldType fieldType;
  private final TimeIntervalType subType;

  /**
   * Creates an instance.
   * 
   * @param type The field type corresponding to this interval type.
   * @param subType The subtype for this type.
   */
  private TimeIntervalType(DateTimeFieldType type, TimeIntervalType subType) {
    this.fieldType = type;
    this.subType = subType;
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
    final DateTimeField field = fieldType.getField(
      ISOChronology.getInstance(
        timeZone
      )
    );
    if (intervalLength < 1
      || intervalLength > field.getMaximumValue()) {
      throw new IllegalArgumentException(
        "Supplied value " + intervalLength
          + " is out of bounds for " + name()
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
  public DateTime getTimeIntervalStart(
    DateTime instant,
    long length
  ) {
    validateValue(instant.getZone(), length);
    // Get the time in the specified timezone
    DateTime periodStart = instant;
    // Clear all the fields for this intervalType and its subtypes
    TimeIntervalType timeIntervalType = this;
    while (timeIntervalType != null) {
      periodStart = timeIntervalType.clearField(periodStart);
      timeIntervalType = timeIntervalType.subType;
    }
    // figure out the which time interval does the instant lie in
    Duration duration = new Duration(periodStart, instant);
    final DurationField durationField = fieldType.getField(
      ISOChronology.getInstance(
        instant.getZone()
      )
    ).getDurationField();
    int diff = durationField.getValue(duration.getMillis());
    long startDelta = (diff / length) * length;
    return periodStart.withFieldAdded(durationField.getType(),
                                      FieldUtils.safeToInt(startDelta));
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

  protected DateTime clearField(DateTime value) {
    return value.withField(
      fieldType,
      fieldType.getField(ISOChronology.getInstance(value.getZone()))
        .getMinimumValue()
    );
  }
}
