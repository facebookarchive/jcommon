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
package com.facebook.concurrency;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Performs a safecast from an exception of type S to an exception of type T if possible.
 *
 * Otherwise, performs wrapping if T has a constructor to wrap a 'cause'.
 *
 * RuntimeExceptions otherwise
 *
 * @param <T>
 */
public class CastOrWrapExceptionHandler<T extends Exception>
  implements ExceptionHandler<T> {
  
  private final Class<T> exceptionClass;

  public CastOrWrapExceptionHandler(Class<T> exceptionClass) {
    this.exceptionClass = exceptionClass;
  }

  @Override
  public <S extends Exception> T handle(S e) {

    if (exceptionClass.isAssignableFrom(e.getClass())) {
      // this cast is in fact safe since we know e is an instanceof Class<T>
      //noinspection unchecked
      return (T)e;
    } else {
      try {
        Constructor<T> constructor = exceptionClass.getConstructor(
          Throwable.class
        );
        // get the exception constructor with one argument
        return constructor.newInstance(e);
      } catch (InstantiationException e1) {
          throw new RuntimeException(e1);
      } catch (IllegalAccessException e1) {
        throw new RuntimeException(e1);
      } catch (InvocationTargetException e1) {
        throw new RuntimeException(e1);
      } catch (NoSuchMethodException e1) {
        throw new RuntimeException(e1);
      }
    }
  }
}
