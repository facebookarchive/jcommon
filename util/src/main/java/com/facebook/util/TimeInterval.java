package com.facebook.util;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.joda.time.DateTime;
import org.joda.time.field.FieldUtils;

/**
 * Represents Time intervals either as durations or periods and abstracts out
 * operations on time intervals in the System.
 * <p/>
 * Durations represent a fixed period of time regardless of when
 * they start or end. ie. 1 day will always be 86400 seconds. Instances
 * that represent duration are constructed via {@link #withMillis(long)}.
 * <p/>
 * Periods represent a period of time but the actual time will depend
 * on when the period starts. For example 1 day will be 23 hours on the first
 * day of DST transition and will be 25 hours on the last day of DST transition.
 * Instances that represent periods are constructed via
 * {@link #withTypeAndLength(TimeIntervalType, int)}.
 * <p>
 *   The main operations abstracted out are the computation of start of
 *   an interval and addition / subtraction of the interval from a time instant.
 * </p>
 */
public class TimeInterval {

  /**
   * An infinite time interval has a length of 0 and always returns the
   * interval start as the start of unix time epoch.
   */
  public static final TimeInterval INFINITE = new TimeInterval(null, 0);
  public static final TimeInterval ZERO = new TimeInterval(null, -1);
  private final long length;
  private final TimeIntervalType type;

  private TimeInterval(TimeIntervalType type, long length) {
    this.type = type;
    this.length = length;
  }


  /**
   * Creates a time interval having a fixed duration of time.
   *
   * @param millis the duration for the interval in milliseconds
   * @return the time interval instance.
   * @throws IllegalArgumentException if millis is less than 1.
   */
  public static TimeInterval withMillis(long millis) {
    validateLength(millis);
    return new TimeInterval(null, millis);
  }

  /**
   * Creates a time interval period based on the supplied type. The actual duration
   * of the period will vary depending on the time instant. The period
   * will take into account DST, varying number of days in a month,
   * leap years, etc.
   * <p>
   * Note that if the interval length doesn't divide the maximum value of the
   * interval type equally, the last interval will be of a smaller length
   * than the previous ones. For example if you specify the interval as
   * 40 seconds, the first interval will have the first 40 seconds in a minute
   * and the second interval will have the remaining 20 seconds in the minute.
   * </p>
   * @param type the time interval type, cannot be null.
   * @param length the length of the interval
   * @return the time interval instance.
   * @throws IllegalArgumentException if length is less than 1.
   */
  public static TimeInterval withTypeAndLength(TimeIntervalType type, int length) {
    if (type == null) {
      throw new IllegalArgumentException("type cannot be null");
    }
    validateLength(length);
    return new TimeInterval(type, length);
  }

  /**
   * Used by jackson for serde
   */
  @JsonCreator
  private static TimeInterval fromJson(
    @JsonProperty("type") TimeIntervalType type,
    @JsonProperty("length") int length
  ) {
    if (type == null) {
      if (length == 0) {
        return INFINITE;
      } else if (length == -1) {
        return ZERO;
      }
    }
    validateLength(length);
    return new TimeInterval(type, length);
  }

  /**
   * Gets the start instant of the time interval that will contain the
   * supplied time instant. Note that the time zone of the supplied instant
   * plays a significant role in computation of the interval.
   *
   * @param instant the time instant
   *
   * @return the start instant of the time interval that will contain the
   * instant in the time zone of the supplied instant. If the TimeInterval is INFINITE
   * unix epoch for the timezone is returned.
   */
  public DateTime getIntervalStart(DateTime instant) {
    // special handling for ZERO and INFINITE
    if (this == ZERO) {
      return instant;
    } else if (this == INFINITE) {
      return new DateTime(1970, 1, 1, 0, 0, 0, 0, instant.getZone());
    }
    
    if (type == null) {
      // unix epoch for the timezone.
      DateTime startOfTime = new DateTime(1970, 1, 1, 0, 0, 0, 0, instant.getZone());
      long intervalStart = ((instant.getMillis() - startOfTime.getMillis()) / length) * length;
      return startOfTime.plus(intervalStart);
    } else {
      return type.getTimeIntervalStart(
        instant,
        length
      );
    }
  }

  /**
   * Adds supplied multiples of this interval to the supplied instant.
   *
   * @param instant the instant that needs to be added to.
   * @param multiple the multiple value
   * @throws IllegalArgumentException if multiple is less than one.
   * @throws UnsupportedOperationException if the function is invoked on an {@link #INFINITE}
   * object
   *
   */
  public DateTime plus(DateTime instant, int multiple) {
    if (this == INFINITE) {
      throw new IllegalStateException(
        "plus() function is not supported on an infinite TimeInterval"
      );
    } else if (this == ZERO) {
      return instant;
    }
    
    validateMultiple(multiple);

    if (type == null) {
      return instant.plus(multiple * getLength());
    } else {
      return instant.plus(
        type.toPeriod(
          FieldUtils.safeMultiplyToInt(multiple, getLength())
        )
      );
    }
  }

  /**
   * Subtracts the supplied multiples of this interval from the supplied instant. If the
   * TimeInterval is {@link #INFINITE} the epoch in the timezone of {@code instant} is returned
   *
   * @param instant the instant to subtract from
   * @param multiple the multiple value
   * @throws IllegalArgumentException if multiple is less than one.
   */
  public DateTime minus(DateTime instant, int multiple) {
    if (this == INFINITE) {
      throw new IllegalStateException(
        "minus() function is not supported on an infinite TimeInterval"
      );
    } else if (this == ZERO) {
      return instant;
    }

    validateMultiple(multiple);

    if (type == null) {
      return instant.minus(multiple * getLength());
    } else {
      return instant.minus(type.toPeriod(
        FieldUtils.safeMultiplyToInt(multiple, getLength())));
    }
  }

  /**
   * If this interval is of type period. Note that for {@link #INFINITE} & {@link #ZERO} time
   * intervals, this method will return false.
   */
  public boolean isPeriod() {
    return type != null;
  }

  /**
   * Returns the length value.
   *
   * @return the length value
   */
  @JsonProperty("length")
  public long getLength() {
    return length;
  }

  @JsonProperty("type")
  TimeIntervalType getType() {
    return type;
  }

  /**
   * Returns the length of the interval in milliseconds.
   *
   * Note that the length is approximate if the interval was constructed
   * via {@link #withTypeAndLength(TimeIntervalType, int)}.
   *
   * Also note that this method returns zero if the TimeInterval is
   * {@link #INFINITE}, -1 if the TimeInterval is {@link #ZERO}.
   *
   * @return the length in millis
   * @deprecated Usage of this method is not encouraged because this only
   * works if the TimeInterval represents a duration. If the time interval
   * is period, this might return unexpected values.
   */
  @Deprecated
  public long toApproxMillis() {
    if (type == null) {
      return length;
    } else {
      return type.toDurationMillis() * length;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final TimeInterval that = (TimeInterval) o;

    if (length != that.length) {
      return false;
    }
    return type == that.type;

  }

  @Override
  public int hashCode() {
    int result = (int) (length ^ (length >>> 32));
    result = 31 * result + (type != null ? type.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "TimeInterval{" +
      "length=" + length +
      ", type=" + type +
      '}';
  }

  private static void validateMultiple(int multiple) {
    if (multiple < 0) {
      throw new IllegalArgumentException("Multiple cannot be less that 0 : " + multiple);
    }
  }

  private static void validateLength(long length) {
    if (length < 1) {
      throw new IllegalArgumentException("length cannot be less than one: " + length);
    }
  }

}
