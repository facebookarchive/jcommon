package com.facebook.util;

import org.apache.log4j.Logger;
import org.joda.time.DateTimeUtils;

public class TimeUtil {
  private static final Logger LOG = Logger.getLogger(TimeUtil.class);

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
      LOG.info(String.format(
        "%s (%b) elapsed time(ms): %d",
        tag,
        success,
        DateTimeUtils.currentTimeMillis() - start
      ));
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
      LOG.info(String.format(
        "%s (%b) elapsed time(ms): %d",
        tag,
        success,
        DateTimeUtils.currentTimeMillis() - start
      ));
    }
  }
}
