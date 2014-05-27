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
package com.facebook.logging;

import org.joda.time.DateTimeUtils;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * decorates a FacebookLogger with  the ability to sample logging at a specified window size
 * (1 per time specified)
 *
 */
public class TimeSamplingLogger implements Logger {
  private final Logger logger;
  private final long windowSizeMillis;
  private final AtomicBoolean logToggle = new AtomicBoolean(false);

  private volatile long lastLoggedMillis = 0;

  public TimeSamplingLogger(Logger logger, long time, TimeUnit timeUnit) {
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
  public boolean isTraceEnabled() {
    return logger.isTraceEnabled();
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
  public void trace(String format, Object... args) {
    if (shouldLog()) {
      logger.trace(format, args);
    }
  }

  @Override
  public void trace(Throwable t, String format, Object... args) {
    if (shouldLog()) {
      logger.trace(t, format, args);
    }
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
  @Deprecated
  public void debug(String message, Throwable throwable) {
    if (shouldLog()) {
      logger.debug(message, throwable);
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
  @Deprecated
  public void info(String message, Throwable throwable) {
    if (shouldLog()) {
      logger.info(message, throwable);
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
  @Deprecated
  public void warn(String message, Throwable throwable) {
    if (shouldLog()) {
      logger.warn(message, throwable);
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

  @Override
  @Deprecated
  public void error(String message, Throwable throwable) {
    if (shouldLog()) {
      logger.error(message, throwable);
    }
  }

  @Override
  public String getName() {
    return logger.getName();
  }
}
