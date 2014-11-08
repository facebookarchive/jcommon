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

import java.io.PrintStream;
import java.util.Formatter;
import java.util.Locale;

class InteractiveStatusPrintStream extends StatusPrintStream {
  private static final String NEW_LINE = System.lineSeparator();

  private final ConsoleStatus status;
  private final Formatter formatter;
  private final ConsoleAppender appender;

  InteractiveStatusPrintStream(PrintStream printStream, ConsoleStatus status, String color) {
    super(NullOutputStream.INSTANCE);
    this.status = status;
    appender = new ConsoleAppender(printStream, status, color);
    formatter = new Formatter(appender, Locale.getDefault());
  }

  @Override
  public void printfln(String format, Object... args) {
    synchronized (status) {
      printf(format, args);
      println();
    }
  }

  @Override
  public void status(String line) {
    status.status(line);
  }

  @Override
  public void status(boolean value) {
    status.status(value);
  }

  @Override
  public void status(char value) {
    status.status(value);
  }

  @Override
  public void status(int value) {
    status.status(value);
  }

  @Override
  public void status(long value) {
    status.status(value);
  }

  @Override
  public void status(float value) {
    status.status(value);
  }

  @Override
  public void status(double value) {
    status.status(value);
  }

  @Override
  public void status(char[] value) {
    status.status(value);
  }

  @Override
  public void status(Object value) {
    status.status(value);
  }

  @Override
  public void statusf(String format, Object... args) {
    status.statusf(format, args);
  }

  @Override
  public void clearStatus() {
    status.clearStatus();
  }

  @Override
  public void flush() {
    appender.flush();
  }

  @Override
  public void close() {
    appender.close();
  }

  @Override
  public boolean checkError() {
    return appender.checkError();
  }

  @Override
  public void write(int b) {
    //noinspection NumericCastThatLosesPrecision
    appender.appendBytes(new byte[]{(byte) b}, 0, 1);
  }

  @Override
  public void write(byte[] buffer, int offset, int length) {
    appender.appendBytes(buffer, offset, length);
  }

  @Override
  public void print(boolean value) {
    print(Boolean.toString(value));
  }

  @Override
  public void print(int value) {
    print(Integer.toString(value));
  }

  @Override
  public void print(long value) {
    print(Long.toString(value));
  }

  @Override
  public void print(float value) {
    print(Float.toString(value));
  }

  @Override
  public void print(double value) {
    print(Double.toString(value));
  }

  @Override
  public void print(Object value) {
    print(String.valueOf(value));
  }

  @Override
  public void print(char value) {
    appender.append(value);
  }

  @Override
  public void print(char[] value) {
    appender.append(new CharArraySequence(value));
  }

  @Override
  public void print(String value) {
    appender.append(value == null ? "null" : value);
  }

  @Override
  public void println() {
    appender.append(NEW_LINE);
  }

  @Override
  public void println(boolean value) {
    println(Boolean.toString(value));
  }

  @Override
  public void println(char value) {
    appender.appendSequences(new SingleCharSequence(value), NEW_LINE);
  }

  @Override
  public void println(int value) {
    println(Integer.toString(value));
  }

  @Override
  public void println(long value) {
    println(Long.toString(value));
  }

  @Override
  public void println(float value) {
    println(Float.toString(value));
  }

  @Override
  public void println(double value) {
    println(Double.toString(value));
  }

  @Override
  public void println(Object value) {
    println(String.valueOf(value));
  }

  @Override
  public void println(char[] value) {
    appender.appendSequences(new CharArraySequence(value), NEW_LINE);
  }

  @Override
  public void println(String value) {
    appender.appendSequences(value, NEW_LINE);
  }

  @Override
  public PrintStream printf(String format, Object... args) {
    return format(format, args);
  }

  @Override
  public PrintStream printf(Locale locale, String format, Object... args) {
    return format(locale, format, args);
  }

  @Override
  public PrintStream format(String format, Object... args) {
    formatter.format(format, args);

    return this;
  }

  @Override
  public PrintStream format(Locale locale, String format, Object... args) {
    formatter.format(locale, format, args);

    return this;
  }

  @Override
  public PrintStream append(CharSequence sequence) {
    appender.append(sequence, 0, sequence.length());

    return this;
  }

  @Override
  public PrintStream append(CharSequence sequence, int start, int end) {
    appender.append(sequence, start, end);

    return this;
  }

  @Override
  public PrintStream append(char c) {
    appender.append(c);

    return this;
  }
}
