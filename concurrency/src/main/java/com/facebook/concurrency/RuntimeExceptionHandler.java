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

/**
 * map any exception to a runtime exception
 */
public class RuntimeExceptionHandler implements ExceptionHandler<RuntimeException> {
  public static final RuntimeExceptionHandler INSTANCE = new RuntimeExceptionHandler();

  @Override
  public <S extends Exception> RuntimeException handle(S e) {
    return new RuntimeException(e.getMessage(), e);
  }
}
