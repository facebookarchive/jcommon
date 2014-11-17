/*
 * Copyright (C) 2014 Facebook, Inc.
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
package com.facebook.tools;

public class ErrorMessage extends RuntimeException {
  private final int errorCode;

  public ErrorMessage(int errorCode, Exception cause, String message) {
    super(message, cause);
    this.errorCode = errorCode;
  }

  public ErrorMessage(int errorCode, Exception cause, String format, Object... args) {
    this(errorCode, cause, String.format(format, args));
  }

  public ErrorMessage(int errorCode, String format, Object... args) {
    this(errorCode, String.format(format, args));
  }

  public ErrorMessage(int errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }

  public ErrorMessage(Exception cause, String message) {
    this(-1, cause, message);
  }

  public ErrorMessage(Exception cause, String format, Object... args) {
    this(-1, cause, format, args);
  }

  public ErrorMessage(String format, Object... args) {
    this(-1, format, args);
  }

  public int getErrorCode() {
    return errorCode;
  }
}
