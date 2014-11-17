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

import com.google.common.base.Joiner;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;

public class TestInteractiveStatusPrintStream {
  private static final String WHITE_ON_RED = "\033[1;37;41m";
  private static final String DEFAULT_COLORS = "\033[0m";

  private ByteArrayOutputStream outputStream;
  private StatusPrintStream out;
  private PrintStreamPlus err;

  @BeforeMethod(alwaysRun = true)
  protected void setUp() throws Exception {
    outputStream = new ByteArrayOutputStream();

    ConsoleStatus status = new ConsoleStatus(new PrintWriter(outputStream));

    out = new InteractiveStatusPrintStream(new PrintStream(outputStream), status, DEFAULT_COLORS);
    err = new InteractiveStatusPrintStream(new PrintStream(outputStream), status, WHITE_ON_RED);
  }

  @Test(groups = "fast")
  public void testStartingWithOut() {
    out.println("Hello, world!");
    out.println("This is a test");
    err.println("Another test...");
    out.println("Goodbye");
    assertOutput(
      "Hello, world!",
      "This is a test",
      "\033[1;37;41m\033[KAnother test...",
      "\033[0m\033[KGoodbye",
      ""
    );
  }

  @Test(groups = "fast")
  public void testStartingWithErr() {
    err.println("Hello, world!");
    err.println("This is a test");
    out.println("Another test...");
    err.println("Goodbye");
    assertOutput(
      "\033[1;37;41m\033[KHello, world!",
      "This is a test",
      "\033[0m\033[KAnother test...",
      "\033[1;37;41m\033[KGoodbye",
      ""
    );
  }

  @Test(groups = "fast")
  public void testStatus() {
    out.print("Hello, world!");
    out.status("Overwrite");
    out.status("Again");
    out.status("and again...");
    out.status("and again!");
    err.println("Also overwrite");
    out.status("Almost done");
    err.println("Good");
    out.status("...");
    err.println("bye!");
    assertOutput(
      "Hello, world!",
      "\r\033[2KOverwrite",
      "\r\033[2KAgain",
      "\r\033[2Kand again...",
      "\r\033[2Kand again!",
      "\r\033[2K\033[1;37;41m\033[KAlso overwrite",
      "",
      "\r\033[0m\033[2KAlmost done",
      "\r\033[2K\033[1;37;41m\033[KGood",
      "",
      "\r\033[0m\033[2K...",
      "\r\033[2K\033[1;37;41m\033[Kbye!",
      ""
    );
  }

  private void assertOutput(String... lines) {
    String actual = outputStream.toString().replace("\033", "\\033").replace("\r", "\n\\r");
    String expected = Joiner.on('\n').join(lines).replace("\033", "\\033").replace("\r", "\\r");

    Assert.assertEquals(actual, expected);
  }
}
