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
package com.facebook.tools.parser;

import com.facebook.tools.ErrorMessage;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;

// TODO add more tests...
public class TestCliParser {
  @Test(groups = "fast")
  public void testSingleValue() {
    CliCommand.Builder command = new CliCommand.Builder("test", "A test");

    command.addOption("-v");
    assertGet(parser(command, "-v", "foo"), "-v", "foo");
    assertGet(parser(command, "-v=foo"), "-v", "foo");
  }

  @Test(groups = "fast")
  public void testDefault() {
    CliCommand.Builder command = new CliCommand.Builder("test", "A test");

    command.addOption("-d")
      .withDefault("foo");
    assertGet(parser(command), "-d", "foo");
  }

  @Test(groups = "fast")
  public void testNullDefault() {
    CliCommand.Builder command = new CliCommand.Builder("test", "A test");

    command.addOption("-d")
      .withDefault(null);
    assertGet(parser(command), "-d", null);
  }

  @Test(groups = "fast")
  public void testMissing() {
    CliCommand.Builder command = new CliCommand.Builder("test", "A test");

    command.addOption("-v");

    try {
      parser(command);
      Assert.fail("expected exception");
    } catch (ErrorMessage e) {
      Assert.assertEquals(e.getMessage(), "Missing required option: -v");
    }
  }

  @Test(groups = "fast")
  public void testUnexpected() {
    CliCommand.Builder command = new CliCommand.Builder("test", "A test");

    command.addOption("-a").withDefault(null);
    command.addOption("-b").withDefault(null);

    try {
      parser(command, "-x", "test", "-a", "hello");
      Assert.fail("expected exception");
    } catch (ErrorMessage e) {
      Assert.assertEquals(e.getMessage(), "Unexpected parameters: -x test");
    }
  }

  @Test(groups = "fast")
  public void testDuplicate() {
    CliCommand.Builder command = new CliCommand.Builder("test", "A test");

    command.addOption("-v");

    try {
      parser(command, "-v", "foo", "-v=bar");
      Assert.fail("expected exception");
    } catch (ErrorMessage e) {
      Assert.assertEquals(e.getMessage(), "Duplicate options: -v=foo, -v=bar");
    }
  }

  @Test(groups = "fast")
  public void testFlag() {
    CliCommand.Builder command = new CliCommand.Builder("test", "A test");

    command.addFlag("-f");
    assertGet(parser(command), "-f", "false");
    assertGet(parser(command, "-f"), "-f", "true");
  }

  @Test(groups = "fast")
  public void testMultiValues() {
    CliCommand.Builder command = new CliCommand.Builder("test", "A test");

    command.addOption("-m")
      .allowMultiple();

    CliParser parser = parser(command, "-m", "val1", "-m", "val2");

    Assert.assertEquals(parser.getMulti("-m"), Arrays.asList("val1", "val2"));
  }

  private static void assertGet(CliParser parser, String option, String expected) {
    String actual = parser.get(option);

    Assert.assertEquals(actual, expected);
  }

  private static CliParser parser(CliCommand.Builder commandBuilder, String... arguments) {
    CliParser parser = new CliParser(commandBuilder.build(), Arrays.asList(arguments));

    parser.verify(new PrintStream(new ByteArrayOutputStream()));

    return parser;
  }
}
