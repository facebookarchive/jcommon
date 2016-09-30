package com.facebook.stats;

import org.joda.time.DateTime;
import org.joda.time.Duration;

/**
 */
public class TimeUtil {
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
