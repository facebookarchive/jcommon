package com.facebook.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logger with efficient var-args support.  Underlying logger is Log4j, but may be swapped to
 * any logger.
 */
public class FacebookLoggerImpl implements FacebookLogger {
  private final Logger logger;

  public FacebookLoggerImpl(Logger logger) {
    this.logger = logger;
  }

  public static FacebookLogger getLogger(Class<?> clazz) {
    Logger logger = LoggerFactory.getLogger(clazz);

    return new FacebookLoggerImpl(logger);
  }

  public static FacebookLogger getLogger(String name) {
    Logger logger = LoggerFactory.getLogger(name);

    return new FacebookLoggerImpl(logger);

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
    if (logger.isDebugEnabled()) {
      String message = String.format(format, args);

      logger.debug(message);
    }
  }

  @Override
  public void debug(Throwable t, String format, Object... args) {
    if (logger.isDebugEnabled()) {
      String message = String.format(format, args);

      logger.debug(message, t);
    }
  }

  @Override
  public void info(String format, Object... args) {
    if (logger.isInfoEnabled()) {
      String message = String.format(format, args);

      logger.info(message);
    }
  }

  @Override
  public void info(Throwable t, String format, Object... args) {
    if (logger.isInfoEnabled()) {
      String message = String.format(format, args);

      logger.info(message, t);
    }
  }

  @Override
  public void warn(String format, Object... args) {
    if (logger.isWarnEnabled()) {
      String message = String.format(format, args);

      logger.warn(message);
    }
  }

  @Override
  public void warn(Throwable t, String format, Object... args) {
    if (logger.isWarnEnabled()) {
      String message = String.format(format, args);

      logger.warn(message, t);
    }
  }

  @Override
  public void error(String format, Object... args) {
    if (logger.isErrorEnabled()) {
      String message = String.format(format, args);

      logger.error(message);
    }
  }

  @Override
  public void error(Throwable t, String format, Object... args) {
    if (logger.isErrorEnabled()) {
      String message = String.format(format, args);

      logger.error(message, t);
    }
  }
}
