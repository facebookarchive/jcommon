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

import com.facebook.tools.ErrorMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static java.nio.charset.StandardCharsets.UTF_8;

public class InputStreamInput implements Input {
  private final BufferedReader reader;

  public InputStreamInput(InputStream inputStream) {
    this(new BufferedReader(new InputStreamReader(inputStream, UTF_8)));
  }

  public InputStreamInput(BufferedReader reader) {
    this.reader = reader;
  }

  @Override
  public int read() {
    try {
      return reader.read();
    } catch (IOException e) {
      throw new ErrorMessage(e, "Error reading input");
    }
  }

  @Override
  public String readLine() {
    try {
      return reader.readLine();
    } catch (IOException e) {
      throw new ErrorMessage(e, "Error reading input");
    }
  }

  @Override
  public Iterator<String> iterator() {
    return new Iterator<String>() {
      private String line;
      private boolean pending = false;

      @Override
      public boolean hasNext() {
        if (pending) {
          return true;
        }

        line = readLine();
        pending = true;

        return line != null;
      }

      @Override
      public String next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }

        pending = false;

        return line;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }
}
