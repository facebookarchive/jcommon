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
package com.facebook.logging.util;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.joda.time.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.Marker;

public class TimeSamplingSLF4JLogger implements Logger {

  private final Logger logger;
  private final long windowSizeMillis;
  private final AtomicBoolean logToggle = new AtomicBoolean(false);

  private volatile long lastLoggedMillis = 0;

  public TimeSamplingSLF4JLogger(Logger logger, long time, TimeUnit timeUnit) {
    this.logger = logger;
    windowSizeMillis = timeUnit.toMillis(time);
  }

  private boolean shouldLog() {
    if (DateTimeUtils.currentTimeMillis() - lastLoggedMillis >= windowSizeMillis
        && logToggle.compareAndSet(false, true)) {
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
  public void debug(String msg) {
    if (shouldLog()) {
      logger.debug(msg);
    }
  }

  @Override
  public void debug(String format, Object arg) {
    if (shouldLog()) {
      logger.debug(format, arg);
    }
  }

  @Override
  public void debug(String format, Object arg1, Object arg2) {
    if (shouldLog()) {
      logger.debug(format, arg1, arg2);
    }
  }

  @Override
  public void debug(String format, Object... args) {
    if (shouldLog()) {
      logger.debug(format, args);
    }
  }

  @Override
  public void debug(String message, Throwable throwable) {
    if (shouldLog()) {
      logger.debug(message, throwable);
    }
  }

  @Override
  public boolean isDebugEnabled(Marker marker) {
    return logger.isDebugEnabled(marker);
  }

  @Override
  public void debug(Marker marker, String msg) {
    if (shouldLog()) {
      logger.debug(marker, msg);
    }
  }

  @Override
  public void debug(Marker marker, String format, Object arg) {
    if (shouldLog()) {
      logger.debug(marker, format, arg);
    }
  }

  @Override
  public void debug(Marker marker, String format, Object arg1, Object arg2) {
    if (shouldLog()) {
      logger.debug(marker, format, arg1, arg2);
    }
  }

  @Override
  public void debug(Marker marker, String format, Object... arguments) {
    if (shouldLog()) {
      logger.debug(marker, format, arguments);
    }
  }

  @Override
  public void debug(Marker marker, String msg, Throwable t) {
    if (shouldLog()) {
      logger.debug(marker, msg, t);
    }
  }

  @Override
  public boolean isInfoEnabled() {
    return logger.isInfoEnabled();
  }

  @Override
  public void info(String msg) {
    if (shouldLog()) {
      logger.debug(msg);
    }
  }

  @Override
  public void info(String format, Object arg) {
    if (shouldLog()) {
      logger.info(format, arg);
    }
  }

  @Override
  public void info(String format, Object arg1, Object arg2) {
    if (shouldLog()) {
      logger.info(format, arg1, arg2);
    }
  }

  @Override
  public boolean isWarnEnabled() {
    return logger.isWarnEnabled();
  }

  @Override
  public void warn(String msg) {
    if (shouldLog()) {
      logger.warn(msg);
    }
  }

  @Override
  public void warn(String format, Object arg) {
    if (shouldLog()) {
      logger.warn(format, arg);
    }
  }

  @Override
  public boolean isErrorEnabled() {
    return logger.isErrorEnabled();
  }

  @Override
  public void error(String msg) {
    if (shouldLog()) {
      logger.error(msg);
    }
  }

  @Override
  public void error(String format, Object arg) {
    if (shouldLog()) {
      logger.error(format, arg);
    }
  }

  @Override
  public void error(String format, Object arg1, Object arg2) {
    if (shouldLog()) {
      logger.error(format, arg1, arg2);
    }
  }

  @Override
  public void info(String format, Object... args) {
    if (shouldLog()) {
      logger.info(format, args);
    }
  }

  @Override
  public void info(String message, Throwable throwable) {
    if (shouldLog()) {
      logger.info(message, throwable);
    }
  }

  @Override
  public boolean isInfoEnabled(Marker marker) {
    return logger.isInfoEnabled(marker);
  }

  @Override
  public void info(Marker marker, String msg) {
    if (shouldLog()) {
      logger.info(marker, msg);
    }
  }

  @Override
  public void info(Marker marker, String format, Object arg) {
    if (shouldLog()) {
      logger.info(marker, format, arg);
    }
  }

  @Override
  public void info(Marker marker, String format, Object arg1, Object arg2) {
    if (shouldLog()) {
      logger.info(marker, format, arg1, arg2);
    }
  }

  @Override
  public void info(Marker marker, String format, Object... arguments) {
    if (shouldLog()) {
      logger.info(marker, format, arguments);
    }
  }

  @Override
  public void info(Marker marker, String msg, Throwable t) {
    if (shouldLog()) {
      logger.info(marker, msg, t);
    }
  }

  @Override
  public void warn(String format, Object... args) {
    if (shouldLog()) {
      logger.warn(format, args);
    }
  }

  @Override
  public void warn(String format, Object arg1, Object arg2) {
    if (shouldLog()) {
      logger.warn(format, arg1, arg2);
    }
  }

  @Override
  public void warn(String message, Throwable throwable) {
    if (shouldLog()) {
      logger.warn(message, throwable);
    }
  }

  @Override
  public boolean isWarnEnabled(Marker marker) {
    return logger.isWarnEnabled(marker);
  }

  @Override
  public void warn(Marker marker, String msg) {
    if (shouldLog()) {
      logger.warn(marker, msg);
    }
  }

  @Override
  public void warn(Marker marker, String format, Object arg) {
    if (shouldLog()) {
      logger.warn(marker, format, arg);
    }
  }

  @Override
  public void warn(Marker marker, String format, Object arg1, Object arg2) {
    if (shouldLog()) {
      logger.warn(marker, format, arg1, arg2);
    }
  }

  @Override
  public void warn(Marker marker, String format, Object... arguments) {
    if (shouldLog()) {
      logger.warn(marker, format, arguments);
    }
  }

  @Override
  public void warn(Marker marker, String msg, Throwable t) {
    if (shouldLog()) {
      logger.warn(marker, msg, t);
    }
  }

  @Override
  public void error(String format, Object... args) {
    if (shouldLog()) {
      logger.error(format, args);
    }
  }

  @Override
  public void error(String message, Throwable throwable) {
    if (shouldLog()) {
      logger.error(message, throwable);
    }
  }

  @Override
  public boolean isErrorEnabled(Marker marker) {
    return logger.isErrorEnabled(marker);
  }

  @Override
  public void error(Marker marker, String msg) {
    if (shouldLog()) {
      logger.error(marker, msg);
    }
  }

  @Override
  public void error(Marker marker, String format, Object arg) {
    if (shouldLog()) {
      logger.error(marker, format, arg);
    }
  }

  @Override
  public void error(Marker marker, String format, Object arg1, Object arg2) {
    if (shouldLog()) {
      logger.error(marker, format, arg1, arg2);
    }
  }

  @Override
  public void error(Marker marker, String format, Object... arguments) {
    if (shouldLog()) {
      logger.error(marker, format, arguments);
    }
  }

  @Override
  public void error(Marker marker, String msg, Throwable t) {
    if (shouldLog()) {
      logger.error(marker, msg, t);
    }
  }

  @Override
  public String getName() {
    return logger.getName();
  }

  @Override
  public boolean isTraceEnabled() {
    return logger.isTraceEnabled();
  }

  @Override
  public void trace(String msg) {
    if (shouldLog()) {
      logger.trace(msg);
    }
  }

  @Override
  public void trace(String format, Object arg) {
    if (shouldLog()) {
      logger.trace(format, arg);
    }
  }

  @Override
  public void trace(String format, Object arg1, Object arg2) {
    if (shouldLog()) {
      logger.trace(format, arg1, arg2);
    }
  }

  @Override
  public void trace(String format, Object... arguments) {
    if (shouldLog()) {
      logger.trace(format, arguments);
    }
  }

  @Override
  public void trace(String msg, Throwable t) {
    if (shouldLog()) {
      logger.trace(msg, t);
    }
  }

  @Override
  public boolean isTraceEnabled(Marker marker) {
    return logger.isTraceEnabled(marker);
  }

  @Override
  public void trace(Marker marker, String msg) {
    if (shouldLog()) {
      logger.trace(marker, msg);
    }
  }

  @Override
  public void trace(Marker marker, String format, Object arg) {
    if (shouldLog()) {
      logger.trace(marker, format, arg);
    }
  }

  @Override
  public void trace(Marker marker, String format, Object arg1, Object arg2) {
    if (shouldLog()) {
      logger.trace(marker, format, arg1, arg2);
    }
  }

  @Override
  public void trace(Marker marker, String format, Object... argArray) {
    if (shouldLog()) {
      logger.trace(marker, format, argArray);
    }
  }

  @Override
  public void trace(Marker marker, String msg, Throwable t) {
    if (shouldLog()) {
      logger.trace(marker, msg, t);
    }
  }
}
