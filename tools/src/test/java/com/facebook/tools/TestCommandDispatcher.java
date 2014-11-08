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

import com.facebook.tools.parser.CliCommand;
import com.facebook.tools.parser.CliParser;
import com.facebook.tools.io.IO;
import com.facebook.tools.io.MockIO;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;

public class TestCommandDispatcher {
  private static final String EXPECTED_FOO =
    "foo\n  test\n\n  -b --bar <option>\n    [Required] Testing\n";
  private static final String EXPECTED_HELP = "help <command_name>\n  Displays help for commands\n";
  private static final String EXPECTED_ALL =
    "foo\n  test\nhelp <command_name>\n  Displays help for commands\n";

  private MockIO io;
  private FooCommand fooCommand;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    io = new MockIO();
    fooCommand = new FooCommand(io);
  }

  @Test(groups = "fast")
  public void testDispatchNoArgs() {
    CommandDispatcher dispatcher = createDispatcher(fooCommand);
    int resultStatus = dispatcher.run();

    assertResult(resultStatus, 0, EXPECTED_ALL, "");
  }

  @Test(groups = "fast")
  public void testDispatchBogusArg() {
    CommandDispatcher dispatcher = createDispatcher(new FooCommand(io));
    int resultStatus = dispatcher.run("bogus");

    assertResult(resultStatus, -1, EXPECTED_ALL + "\n", "Unknown commandName: bogus\n");
  }

  @Test(groups = "fast")
  public void testDispatchHelp() {
    CommandDispatcher dispatcher = createDispatcher(fooCommand);
    int resultStatus = dispatcher.run("help");

    assertResult(resultStatus, 0, EXPECTED_ALL, "");
  }

  @Test(groups = "fast")
  public void testDispatchHelpFoo() {
    CommandDispatcher dispatcher = createDispatcher(fooCommand);
    int resultStatus = dispatcher.run("help", "foo");

    assertResult(resultStatus, 0, EXPECTED_FOO, "");
  }

  @Test(groups = "fast")
  public void testDispatchHelpHelp() {
    CommandDispatcher dispatcher = createDispatcher(fooCommand);
    int resultStatus = dispatcher.run("help", "help");

    assertResult(resultStatus, 0, EXPECTED_HELP, "");
  }

  @Test(groups = "fast")
  public void testDispatchFoo() {
    CommandDispatcher dispatcher = createDispatcher(fooCommand);
    int resultStatus = dispatcher.run("foo", "--bar", "test");

    assertResult(resultStatus, 0, "I am foo\n", "oof ma I\n");
  }

  @Test(groups = "fast")
  public void testDispatchFooMissingRequired() {
    CommandDispatcher dispatcher = createDispatcher(fooCommand);
    int resultStatus = dispatcher.run("foo");

    assertResult(resultStatus, -1, EXPECTED_FOO + "\n", "Missing required option: --bar\n");
  }

  @Test(groups = "fast")
  public void testDuplicateArg() {
    CommandDispatcher dispatcher = createDispatcher(fooCommand);
    int resultStatus = dispatcher.run("foo", "-b", "bar", "--bar=baz");

    assertResult(resultStatus, -1, EXPECTED_FOO + "\n", "Duplicate options: -b=bar, --bar=baz\n");
  }

  private CommandDispatcher createDispatcher(CommandBuilder... commands) {
    return new CommandDispatcher(io, Arrays.asList(commands));
  }

  private void assertResult(
    int actualResult, int expectedResult, String expectedOut, String expectedErr
  ) {
    String out = io.getOut();
    String err = io.getErr();

    Assert.assertEquals(actualResult, expectedResult, out + err);
    Assert.assertEquals(out, expectedOut);
    Assert.assertTrue(err.startsWith(expectedErr));
  }

  private static class FooCommand implements CommandBuilder {
    private final IO io;

    private FooCommand(IO io) {
      this.io = io;
    }

    @Override
    public CliCommand defineCommand() {
      CliCommand.Builder builder = new CliCommand.Builder("foo", "test");

      builder.addOption("-b", "--bar").withDescription("Testing");

      return builder.build();
    }

    @Override
    public void runCommand(CliParser parser) {
      io.out.println("I am foo");
      io.err.println("oof ma I");
    }
  }
}
