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

/**
 * Facebook logging interface that is similar to log4j, but adds delayed format processing
 *
 **/

public interface Logger {
  public boolean isTraceEnabled();

  public boolean isDebugEnabled();

  public boolean isInfoEnabled();

  public boolean isWarnEnabled();

  public boolean isErrorEnabled();

  public void trace(String format, Object... args);

  public void trace(Throwable t, String format, Object... args);

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


  /**
   * @return the name of the logger instance
   */
  public String getName();
}
