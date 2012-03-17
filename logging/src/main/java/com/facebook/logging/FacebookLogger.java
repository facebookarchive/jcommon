package com.facebook.logging;

/**
 * Facebook logging interface that is similar to log4j, but adds delayed format processing
 */
public interface FacebookLogger {
  public boolean isDebugEnabled();

  public boolean isInfoEnabled();

  public boolean isWarnEnabled();

  public boolean isErrorEnabled();

  public void debug(String format, Object... args);

  public void debug(Throwable t, String format, Object... args);

  public void info(String format, Object... args);

  public void info(Throwable t, String format, Object... args);

  public void warn(String format, Object... args);

  public void warn(Throwable t, String format, Object... args);

  public void error(String format, Object... args);

  public void error(Throwable t, String format, Object... args);
}
