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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that captures the convenient methods (that is not found in any of the third-party logging
 * frameworks) for logging.
 */
public class LoggingUtil {

  /**
   * Returns a logger for the calling class or context.
   * <p/>
   * The fully-qualified name of that class is used to get an slf4j logger, which is then wrapped.
   * Typical usage is to use this method to initialize a static member variable, e.g.,
   * {@code private static final Logger LOG = LoggingUtil.getClassLogger();}
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

    return LoggerFactory.getLogger(name);
  }
}
