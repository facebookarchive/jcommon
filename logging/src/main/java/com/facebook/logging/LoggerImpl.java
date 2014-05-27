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

import org.slf4j.LoggerFactory;

/**
 * Logger with efficient var-args support.  Underlying logger is sl4j, but may be swapped to
 * any logger.
 */
public class LoggerImpl implements Logger {
  private final org.slf4j.Logger logger;

  public LoggerImpl(org.slf4j.Logger logger) {
    this.logger = logger;
  }

  public static Logger getLogger(Class<?> clazz) {
    org.slf4j.Logger logger = LoggerFactory.getLogger(clazz);

    return new LoggerImpl(logger);
  }

  public static Logger getLogger(String name) {
    org.slf4j.Logger logger = LoggerFactory.getLogger(name);

    return new LoggerImpl(logger);
  }

  /**
   * Returns a logger for the calling class or context.
   * <p/>
   * The fully-qualified name of that class is used to get an slf4j logger, which is then wrapped.
   * Typical usage is to use this method to initialize a static member variable, e.g.,
   * {@code private static final Logger LOG = Logger.getLogger();}
   * <p/>
   * As getStackTrace() isn't super cheap, this is not the sort of thing you want (or need)
   * to be doing hundreds of times a second;
   *
   * @return a logger for the current scope
   */
  public static Logger getClassLogger() {
    StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
    StackTraceElement element = stacktrace[2];
    String name = element.getClassName();

    return new LoggerImpl(LoggerFactory.getLogger(name));
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
    if (logger.isTraceEnabled()) {
      logger.trace(formatMessage(format, args));
    }
  }

  @Override
  public void trace(Throwable t, String format, Object... args) {
    if (logger.isTraceEnabled()) {
      logger.trace(formatMessage(format, args), t);
    }
  }

  @Override
  public void debug(String format, Object... args) {
    if (logger.isDebugEnabled()) {
      String message = formatMessage(format, args);

      logger.debug(message);
    }
  }

  @Override
  public void debug(Throwable t, String format, Object... args) {
    if (logger.isDebugEnabled()) {
      String message = formatMessage(format, args);

      logger.debug(message, t);
    }
  }

  @Override
  @Deprecated
  public void debug(String message, Throwable throwable) {
    if (logger.isDebugEnabled()) {
      logger.debug(message, throwable);
    }

  }

  @Override
  public void info(String format, Object... args) {
    if (logger.isInfoEnabled()) {
      String message = formatMessage(format, args);

      logger.info(message);
    }
  }

  @Override
  public void info(Throwable t, String format, Object... args) {
    if (logger.isInfoEnabled()) {
      String message = formatMessage(format, args);

      logger.info(message, t);
    }
  }

  @Override
  @Deprecated
  public void info(String message, Throwable throwable) {
    if (logger.isInfoEnabled()) {
      logger.warn(message, throwable);
    }
  }

  @Override
  public void warn(String format, Object... args) {
    if (logger.isWarnEnabled()) {
      String message = formatMessage(format, args);

      logger.warn(message);
    }
  }

  @Override
  public void warn(Throwable t, String format, Object... args) {
    if (logger.isWarnEnabled()) {
      String message = formatMessage(format, args);

      logger.warn(message, t);
    }
  }

  @Override
  @Deprecated
  public void warn(String message, Throwable throwable) {
    if (logger.isWarnEnabled()) {
      logger.warn(message, throwable);
    }
  }

  @Override
  public void error(String format, Object... args) {
    if (logger.isErrorEnabled()) {
      String message = formatMessage(format, args);

      logger.error(message);
    }
  }

  @Override
  public void error(Throwable t, String format, Object... args) {
    if (logger.isErrorEnabled()) {
      String message = formatMessage(format, args);

      logger.error(message, t);
    }
  }

  @Override
  @Deprecated
  public void error(String message, Throwable throwable) {
    if (logger.isErrorEnabled()) {
      logger.error(message, throwable);
    }
  }

  @Override
  public String getName() {
    return logger.getName();
  }

  private String formatMessage(String format, Object[] args) {
    return args.length == 0 ? format : String.format(format, args);
  }
}
