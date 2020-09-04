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

import com.facebook.util.exceptions.ExceptionHandler;
import java.util.concurrent.Callable;

public class CallableSnapshotFunctionImpl<I, O, E extends Exception>
    implements CallableSnapshotFunction<I, O, E> {
  private final ValueFactory<I, O, E> valueFactory;
  private final ExceptionHandler<E> exceptionHandler;

  public CallableSnapshotFunctionImpl(
      ValueFactory<I, O, E> valueFactory, ExceptionHandler<E> exceptionHandler) {
    this.valueFactory = valueFactory;
    this.exceptionHandler = exceptionHandler;
  }

  public CallableSnapshotFunctionImpl(ValueFactory<I, O, E> valueFactory) {
    // We can cast exceptions because the value factory declares which type
    // of exceptions it can throw on creation
    this(valueFactory, new CastingExceptionHandler<>());
  }

  @Override
  public CallableSnapshot<O, E> apply(I input) {
    return new CallableSnapshot<>(
        new Callable<O>() {
          @Override
          public O call() throws E {
            return valueFactory.create(input);
          }
        },
        exceptionHandler);
  }
}
