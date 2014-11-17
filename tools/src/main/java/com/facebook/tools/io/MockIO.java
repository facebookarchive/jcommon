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

import com.facebook.tools.subprocess.SubprocessBuilder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class MockIO extends IO {
  private final ByteArrayOutputStream out;
  private final ByteArrayOutputStream err;

  public MockIO(String input, SubprocessBuilder subprocess) {
    this(new ByteArrayOutputStream(), new ByteArrayOutputStream(), input, subprocess);
  }

  public MockIO(SubprocessBuilder subprocess) {
    this("", subprocess);
  }

  public MockIO(String input) {
    this(input, null);
  }


  public MockIO() {
    this("", null);
  }

  public String getOut() {
    return out.toString();
  }

  public String getErr() {
    return err.toString();
  }

  private MockIO(
    ByteArrayOutputStream out, ByteArrayOutputStream err, String input, SubprocessBuilder subprocess
  ) {
    super(
      new PrintStream(out),
      new PrintStream(err),
      new InputStreamInput(new ByteArrayInputStream(input.getBytes())),
      subprocess
    );
    this.out = out;
    this.err = err;
  }
}
