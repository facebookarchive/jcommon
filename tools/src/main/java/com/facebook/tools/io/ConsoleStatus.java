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
package com.facebook.tools.io;

import java.io.PrintWriter;

class ConsoleStatus implements Status {
  private static final String ERASE_TO_END_OF_LINE = "\033[K";
  private static final String RESET_AND_ERASE_LINE = "\r\033[0m\033[2K";
  private static final String ERASE_LINE = "\r\033[2K";
  private static final String STATUS_COLOR = "\033[0m";

  private final PrintWriter out;

  private boolean currentLineIsStatus;
  private String color = STATUS_COLOR;

  ConsoleStatus(PrintWriter out) {
    this.out = out;
  }

  @Override
  public synchronized void status(String line) {
    eraseLine();
    out.print(line);
    out.flush();
    currentLineIsStatus = true;
  }

  @Override
  public void status(boolean value) {
    status(Boolean.toString(value));
  }

  @Override
  public void status(char value) {
    status(Character.toString(value));
  }

  @Override
  public void status(int value) {
    status(Integer.toString(value));
  }

  @Override
  public void status(long value) {
    status(Long.toString(value));
  }

  @Override
  public void status(float value) {
    status(Float.toString(value));
  }

  @Override
  public void status(double value) {
    status(Double.toString(value));
  }

  @Override
  public void status(char[] value) {
    status(new String(value));
  }

  @Override
  public void status(Object value) {
    status(String.valueOf(value));
  }

  @Override
  public void statusf(String format, Object... args) {
    status(String.format(format, args));
  }

  @Override
  public synchronized void clearStatus() {
    if (currentLineIsStatus) {
      currentLineIsStatus = false;
      eraseLine();
    }
  }

  synchronized void setColor(String color) {
    if (!this.color.equals(color)) {
      out.flush();
      out.write(color);
      //noinspection AccessToStaticFieldLockedOnInstance
      out.write(ERASE_TO_END_OF_LINE);
      out.flush();
      this.color = color;
    }
  }

  private void eraseLine() {
    out.flush();

    if (color.equals(STATUS_COLOR)) {
      //noinspection AccessToStaticFieldLockedOnInstance
      out.write(ERASE_LINE);
    } else {
      //noinspection AccessToStaticFieldLockedOnInstance
      out.write(RESET_AND_ERASE_LINE);
    }

    out.flush();
    color = STATUS_COLOR;
  }
}
