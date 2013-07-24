package com.facebook.stats;

/**
 * A drop-in replacement for the joda time DateTimeUtils but uses System.nanoTime
 * under the hood. This is intended to be a bridge for the legacy code written
 * against DateTimeUtils.currentTimeMills().
 */
public class DateTimeUtils {

  private static boolean isFixed = false;
  private static long fixedValue;

  public static void setCurrentMillisFixed(long value) {
    isFixed = true;
    fixedValue = value;
  }

  public static long currentTimeMillis() {
    return isFixed ? fixedValue : System.nanoTime() / 1000000;
  }

  public static void setCurrentMillisSystem() {
    isFixed = false;
  }
}
