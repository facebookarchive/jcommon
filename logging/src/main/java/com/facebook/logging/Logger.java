package com.facebook.logging;

/**
 * Facebook logging interface that is similar to log4j, but adds delayed format processing
 */
public interface Logger {
  public boolean isDebugEnabled();

  public boolean isInfoEnabled();

  public boolean isWarnEnabled();

  public boolean isErrorEnabled();

  public void debug(String format, Object... args);

  public void debug(Throwable t, String format, Object... args);

  /**
   * @deprecated you might prefer {@link #debug(Throwable, String)}
   */
  @Deprecated
  public void debug(String message, Throwable throwable);

  public void info(String format, Object... args);

  public void info(Throwable t, String format, Object... args);

  /**
   * @deprecated you might prefer {@link #info(Throwable, String)}
   */
  @Deprecated
  public void info(String message, Throwable throwable);

  public void warn(String format, Object... args);

  public void warn(Throwable t, String format, Object... args);

  /**
   * @deprecated you might prefer {@link #warn(Throwable, String)}
   */
  @Deprecated
  public void warn(String message, Throwable throwable);

  public void error(String format, Object... args);

  public void error(Throwable t, String format, Object... args);

  /**
   * @deprecated you might prefer {@link #error(Throwable, String)}
   */
  @Deprecated
  public void error(String message, Throwable throwable);

}
