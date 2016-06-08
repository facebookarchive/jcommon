/*
 * Copyright (C) 2016 Facebook, Inc.
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

import com.google.common.base.Function;

import java.util.concurrent.atomic.AtomicReference;

import com.facebook.util.ExtRunnable;
import com.facebook.util.exceptions.ExceptionHandler;

class ShortCircuitRunnable<E extends Exception> implements Function<ExtRunnable<E>, Runnable> {
  private final AtomicReference<E> exception;
  private final ExceptionHandler<E> exceptionHandler;

  ShortCircuitRunnable(AtomicReference<E> exception, ExceptionHandler<E> exceptionHandler) {
    this.exception = exception;
    this.exceptionHandler = exceptionHandler;
  }

  @Override
  public Runnable apply(final ExtRunnable<E> task) {
    return () -> {
      try {
        // short-circuit if other tasksIter failed
        if (exception.get() == null) {
          task.run();
        }
      } catch (Exception e) {
        exception.compareAndSet(null, exceptionHandler.handle(e));
      }
    };
  }
}
