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

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.joda.time.DateTimeUtils;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Logger wrapper to down-sample frequent logs to one log per specified time
 */
public class TimeSamplingLog4jLogger {
  private final Logger logger;
  private final long windowSizeMillis;
  private final AtomicBoolean logToggle = new AtomicBoolean(false);

  private volatile long lastLoggedMillis = 0;

  public TimeSamplingLog4jLogger(Logger logger, long time, TimeUnit timeUnit) {
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

  public void debug(String format, Object ... args) {
    if (shouldLog() && logger.isDebugEnabled()) {
      String message = String.format(format, args);

      logger.debug(message);
    }
  }

  public void debug(Throwable t, String format, Object ... args) {
    if (shouldLog() && logger.isDebugEnabled()) {    
      String message = String.format(format, args);

      logger.debug(message, t);
    }
  }

  public void info(String format, Object ... args) {
    if (shouldLog() && logger.isInfoEnabled()) {
      String message = String.format(format, args);

      logger.info(message);
    }
  }

  public void info(Throwable t, String format, Object ... args) {
    if (shouldLog() && logger.isInfoEnabled()) {    
      String message = String.format(format, args);

      logger.info(message, t);
    }
  }

  public void warn(String format, Object ... args) {
    if (shouldLog() && logger.isEnabledFor(Level.WARN)) {
      String message = String.format(format, args);

      logger.warn(message);
    }
  }

  public void warn(Throwable t, String format, Object ... args) {
    if (shouldLog() && logger.isEnabledFor(Level.WARN)) {
      String message = String.format(format, args);

      logger.warn(message, t);
    }
  }
  
  public void error(String format, Object ... args) {
    if (shouldLog() && logger.isEnabledFor(Level.ERROR)) {
      String message = String.format(format, args);

      logger.error(message);
    }
  }

  public void error(Throwable t, String format, Object ... args) {
    if (shouldLog() && logger.isEnabledFor(Level.ERROR)) {
      String message = String.format(format, args);

      logger.error(message, t);
    }
  }
}
