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
package com.facebook.util;

import java.lang.reflect.Constructor;

public class ExceptionUtils {
  private ExceptionUtils() {
    throw new AssertionError("Not instantiable: " + ExceptionUtils.class);
  }

  public static <T extends Exception, S extends Exception> T wrap(S e, Class<T> clazz) {
    if (clazz.isAssignableFrom(e.getClass())) {
      return clazz.cast(e);
    }

    try {
      Constructor<T> constructor = clazz.getConstructor(Throwable.class);

      // get the exception constructor with one argument
      return constructor.newInstance(e);
    } catch (RuntimeException exception) {
      throw exception;
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }
}
