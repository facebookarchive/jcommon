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

import java.util.concurrent.Callable;

import com.facebook.util.exceptions.ExceptionHandler;

public class CallableSnapshot<V, E extends Exception> {
  private V value = null;
  private E exception = null;

  private CallableSnapshot(E exception) {
    this.exception = exception;
  }

  public CallableSnapshot(
    Callable<V> callable, ExceptionHandler<E> exceptionHandler
  ) {
    try {
      value = callable.call();
    } catch (Exception e) {
      exception = exceptionHandler.handle(e);
    }
  }

  public static <V, E extends Exception> CallableSnapshot<V, E> createWithException(E exception) {
    return new CallableSnapshot<>(exception);
  }

  public V get() throws E {
    if (exception != null) {
      throw exception;
    }
    return value;
  }

  public E getException() {
    return exception;
  }
}
