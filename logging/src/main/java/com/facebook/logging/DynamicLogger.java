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

public class DynamicLogger implements Logger {
  private final LogProvider logProvider;

  public DynamicLogger(LogProvider logProvider) {
    this.logProvider = logProvider;
  }

  private Logger getLogger() {
    return logProvider.get();
  }

  @Override
  public boolean isTraceEnabled() {
    return getLogger().isTraceEnabled();
  }

  @Override
  public boolean isDebugEnabled() {
    return getLogger().isDebugEnabled();
  }

  @Override
  public boolean isInfoEnabled() {
    return getLogger().isInfoEnabled();
  }

  @Override
  public boolean isWarnEnabled() {
    return getLogger().isWarnEnabled();
  }

  @Override
  public boolean isErrorEnabled() {
    return getLogger().isErrorEnabled();
  }

  @Override
  public void trace(String format, Object... args) {
    getLogger().trace(format, args);
  }

  @Override
  public void trace(Throwable t, String format, Object... args) {
    getLogger().trace(t, format, args);
  }

  @Override
  public void debug(String format, Object... args) {
    getLogger().debug(format, args);
  }

  @Override
  public void debug(Throwable t, String format, Object... args) {
    getLogger().debug(t, format, args);
  }

  @Override
  @Deprecated
  public void debug(String message, Throwable throwable) {
    getLogger().debug(message, throwable);
  }

  @Override
  public void info(String format, Object... args) {
    getLogger().info(format, args);
  }

  @Override
  public void info(Throwable t, String format, Object... args) {
    getLogger().info(t, format, args);
  }

  @Override
  @Deprecated
  public void info(String message, Throwable throwable) {
    getLogger().info(message, throwable);
  }

  @Override
  public void warn(String format, Object... args) {
    getLogger().warn(format, args);
  }

  @Override
  public void warn(Throwable t, String format, Object... args) {
    getLogger().warn(t, format, args);
  }

  @Override
  @Deprecated
  public void warn(String message, Throwable throwable) {
    getLogger().warn(message, throwable);
  }

  @Override
  public void error(String format, Object... args) {
    getLogger().error(format, args);
  }

  @Override
  public void error(Throwable t, String format, Object... args) {
    getLogger().error(t, format, args);
  }

  @Override
  @Deprecated
  public void error(String message, Throwable throwable) {
    getLogger().error(message, throwable);
  }

  @Override
  public String getName() {
    return getLogger().getName();
  }
}
