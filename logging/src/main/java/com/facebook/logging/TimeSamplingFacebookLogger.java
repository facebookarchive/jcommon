package com.facebook.logging;

import org.joda.time.DateTimeUtils;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * decorates a FacebookLogger with  the ability to sample logging at a specified window size 
 * (1 per time specified)
 **/
public class TimeSamplingFacebookLogger implements Logger {
  private final Logger logger;
  private final long windowSizeMillis;
  private final AtomicBoolean logToggle = new AtomicBoolean(false);

  private volatile long lastLoggedMillis = 0;

  public TimeSamplingFacebookLogger(Logger logger, long time, TimeUnit timeUnit) {
    this.logger = logger;
    windowSizeMillis = timeUnit.toMillis(time);
  }

  private boolean shouldLog() {
    if (DateTimeUtils.currentTimeMillis() - lastLoggedMillis >= windowSizeMillis
      && logToggle.compareAndSet(false, true)
      ) {
      try {
        lastLoggedMillis = DateTimeUtils.currentTimeMillis();

        return true;
      } finally {
        logToggle.set(false);
      }
    }

    return false;
  }

  @Override
  public boolean isDebugEnabled() {
    return logger.isDebugEnabled();
  }

  @Override
  public boolean isInfoEnabled() {
    return logger.isInfoEnabled();
  }

  @Override
  public boolean isWarnEnabled() {
    return logger.isWarnEnabled();
  }

  @Override
  public boolean isErrorEnabled() {
    return logger.isErrorEnabled();
  }

  @Override
  public void debug(String format, Object... args) {
    if (shouldLog()) {
      logger.debug(format, args);
    }
  }

  @Override
  public void debug(Throwable t, String format, Object... args) {
    if (shouldLog()) {
      logger.debug(t, format, args);
    }
  }

  @Override
  public void info(String format, Object... args) {
    if (shouldLog()) {
      logger.info(format, args);
    }
  }

  @Override
  public void info(Throwable t, String format, Object... args) {
    if (shouldLog()) {
      logger.info(t, format, args);
    }
  }

  @Override
  public void warn(String format, Object... args) {
    if (shouldLog()) {
      logger.warn(format, args);
    }
  }

  @Override
  public void warn(Throwable t, String format, Object... args) {
    if (shouldLog()) {
      logger.warn(t, format, args);
    }
  }

  @Override
  public void error(String format, Object... args) {
    if (shouldLog()) {
      logger.error(format, args);
    }
  }

  @Override
  public void error(Throwable t, String format, Object... args) {
    if (shouldLog()) {
      logger.error(t, format, args);
    }
  }
}
