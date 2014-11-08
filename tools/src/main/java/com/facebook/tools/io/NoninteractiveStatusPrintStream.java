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
import java.util.Locale;

class NoninteractiveStatusPrintStream extends StatusPrintStream {
  private final PrintStream printStream;

  NoninteractiveStatusPrintStream(PrintStream printStream) {
    super(NullOutputStream.INSTANCE);
    this.printStream = printStream;
  }

  @Override
  public void printfln(String format, Object... args) {
    printf(format, args);
    println();
  }

  @Override
  public void status(String line) {
  }

  @Override
  public void status(boolean value) {
  }

  @Override
  public void status(char value) {
  }

  @Override
  public void status(int value) {
  }

  @Override
  public void status(long value) {
  }

  @Override
  public void status(float value) {
  }

  @Override
  public void status(double value) {
  }

  @Override
  public void status(char[] value) {
  }

  @Override
  public void status(Object value) {
  }

  @Override
  public void statusf(String format, Object... args) {
  }

  @Override
  public void clearStatus() {
  }

  @Override
  public void flush() {
    printStream.flush();
  }

  @Override
  public void close() {
    printStream.close();
  }

  @Override
  public boolean checkError() {
    return printStream.checkError();
  }

  @Override
  public void write(int b) {
    printStream.write(b);
  }

  @Override
  public void write(byte[] buffer, int offset, int length) {
    printStream.write(buffer, offset, length);
  }

  @Override
  public void print(boolean b) {
    printStream.print(b);
  }

  @Override
  public void print(char c) {
    printStream.print(c);
  }

  @Override
  public void print(int i) {
    printStream.print(i);
  }

  @Override
  public void print(long l) {
    printStream.print(l);
  }

  @Override
  public void print(float f) {
    printStream.print(f);
  }

  @Override
  public void print(double d) {
    printStream.print(d);
  }

  @Override
  public void print(char[] s) {
    printStream.print(s);
  }

  @Override
  public void print(String s) {
    printStream.print(s);
  }

  @Override
  public void print(Object obj) {
    printStream.print(obj);
  }

  @Override
  public void println() {
    printStream.println();
  }

  @Override
  public void println(boolean value) {
    printStream.println(value);
  }

  @Override
  public void println(char value) {
    printStream.println(value);
  }

  @Override
  public void println(int value) {
    printStream.println(value);
  }

  @Override
  public void println(long value) {
    printStream.println(value);
  }

  @Override
  public void println(float value) {
    printStream.println(value);
  }

  @Override
  public void println(double value) {
    printStream.println(value);
  }

  @Override
  public void println(char[] value) {
    printStream.println(value);
  }

  @Override
  public void println(String value) {
    printStream.println(value);
  }

  @Override
  public void println(Object value) {
    printStream.println(value);
  }

  @Override
  public PrintStream printf(String format, Object... args) {
    printStream.printf(format, args);
    
    return this;
  }

  @Override
  public PrintStream printf(Locale locale, String format, Object... args) {
    printStream.printf(locale, format, args);
    
    return this;
  }

  @Override
  public PrintStream format(String format, Object... args) {
    printStream.format(format, args);
    
    return this;
  }

  @Override
  public PrintStream format(Locale locale, String format, Object... args) {
    printStream.format(locale, format, args);
    
    return this;
  }

  @Override
  public PrintStream append(CharSequence sequence) {
    printStream.append(sequence);
    
    return this;
  }

  @Override
  public PrintStream append(CharSequence sequence, int start, int end) {
    printStream.append(sequence, start, end);
    
    return this;
  }

  @Override
  public PrintStream append(char c) {
    printStream.append(c);
    
    return this;
  }
}
