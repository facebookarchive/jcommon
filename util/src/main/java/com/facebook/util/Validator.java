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

import com.google.common.base.Preconditions;

public class Validator<T extends Exception> {
  private final Class<T> clazz;

  public Validator(Class<T> clazz) {
    this.clazz = clazz;
  }

  public void checkState(boolean expression) throws T {
    try {
      Preconditions.checkState(expression);
    } catch (Exception e) {
      throw ExceptionUtils.wrap(e, clazz);
    }
  }

  public void checkState(boolean expression, String message) throws T {
    try {
      Preconditions.checkState(expression, message);
    } catch (Exception e) {
      throw ExceptionUtils.wrap(e, clazz);
    }
  }

  public void checkState(boolean expression, String message, Object... errorMessageArgs) throws T {
    try {
      Preconditions.checkState(expression, message, errorMessageArgs);
    } catch (Exception e) {
      throw ExceptionUtils.wrap(e, clazz);
    }
  }
}
