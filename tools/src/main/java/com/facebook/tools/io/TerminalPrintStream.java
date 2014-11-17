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

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;

public class TerminalPrintStream extends PrintStream {
  private final boolean interactive;
  private boolean currentLineIsStatus = false;

  private TerminalPrintStream(OutputStream out, boolean interactive) {
    super(out, true);
    this.interactive = interactive;
  }

  public static TerminalPrintStream interactive(OutputStream out) {
    return new TerminalPrintStream(out, true);
  }

  public static TerminalPrintStream noninteractive(OutputStream out) {
    return new TerminalPrintStream(out, false);
  }

  @Override
  public void write(int b) {
    eraseStatusLine();
    super.write(b);
  }

  @Override
  public void write(byte[] buffer, int offset, int length) {
    if (currentLineIsStatus) {
      // HACK eraseStatusLine() modifies buffer, so we need to copy/restore
      byte[] bufferCopy = Arrays.copyOf(buffer, buffer.length);

      eraseStatusLine();
      buffer = bufferCopy;
    }

    super.write(buffer, offset, length);
  }

  public void printfln(String format, Object... args) {
    printf(format, args);
    println();
  }

  public void status(String line) {
    if (interactive) {
      print(line);
      flush();
      currentLineIsStatus = true;
    }
  }

  public void status(boolean b) {
    status(Boolean.toString(b));
  }

  public void status(char c) {
    status(Character.toString(c));
  }

  public void status(int i) {
    status(Integer.toString(i));
  }

  public void status(long l) {
    status(Long.toString(l));
  }

  public void status(float f) {
    status(Float.toString(f));
  }

  public void status(double d) {
    status(Double.toString(d));
  }

  public void status(char[] s) {
    status(new String(s));
  }

  public void status(Object obj) {
    status(String.valueOf(obj));
  }

  public void statusf(String format, Object... args) {
    status(String.format(format, args));
  }

  private void eraseStatusLine() {
    if (currentLineIsStatus) {
      currentLineIsStatus = false;
      flush();
      print("\r\033[2K");
      flush();
    }
  }
}
