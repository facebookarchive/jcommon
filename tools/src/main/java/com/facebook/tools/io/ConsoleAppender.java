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

import java.io.Closeable;
import java.io.PrintStream;

class ConsoleAppender implements Appendable, Closeable {
  private final PrintStream delegate;
  private final ConsoleStatus status;
  private final String color;

  ConsoleAppender(PrintStream delegate, ConsoleStatus status, String color) {
    this.delegate = delegate;
    this.status = status;
    this.color = color;
  }

  @Override
  public ConsoleAppender append(CharSequence sequence) {
    appendSequences(sequence);

    return this;
  }

  @Override
  public ConsoleAppender append(CharSequence sequence, int start, int end) {
    return appendSequences(sequence.subSequence(start, end));
  }

  @Override
  public Appendable append(char c) {
    return appendSequences(new SingleCharSequence(c));
  }

  @Override
  public void close() {
    delegate.close();
  }

  ConsoleAppender appendSequences(CharSequence... sequences) {
    synchronized (status) {
      status.clearStatus();
      status.setColor(color);

      for (CharSequence sequence : sequences) {
        delegate.print(sequence);
      }
    }

    return this;
  }

  ConsoleAppender appendBytes(byte[] bytes, int offset, int length) {
    synchronized (status) {
      status.clearStatus();
      status.setColor(color);
      delegate.write(bytes, offset, length);
    }

    return this;
  }

  boolean checkError() {
    return delegate.checkError();
  }

  void flush() {
    delegate.flush();
  }
}
