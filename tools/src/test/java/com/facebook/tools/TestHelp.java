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
import com.facebook.tools.io.MockIO;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TestHelp {
  private MockIO io;
  private Help help;

  @BeforeMethod(groups = "fast")
  public void setUp() {
    io = new MockIO();

    CliCommand.Builder noOptions = new CliCommand.Builder("foo", "My awesome foo command");
    CliCommand.Builder oneOption = new CliCommand.Builder(
      "bar", "My awesome bar command", "(even more awesome than foo)"
    );
    CliCommand.Builder severalOptions = new CliCommand.Builder("baz", "Meh");
    CliCommand.Builder withParameter = new CliCommand.Builder("pram", "This has a param");
    CliCommand.Builder withNotes = new CliCommand.Builder("noted", "I have notes");

    oneOption.addOption("-b", "--bar").withMetavar("bar");
    severalOptions.addOption("-x").withMetavar("hello").withDescription("Make everything awesome");
    severalOptions.addFlag("-f").withDescription("A flag of some sort", "that enables something");
    severalOptions.addOption("-d", "--default").withDescription("Default").withDefault("testing");
    withParameter.addParameter("cool");
    withNotes.addOption("-b", "--bar").withMetavar("bar").withExample("hello", "goodbye");
    withNotes.withNotes("Yes, that's the same option", "(it's a good option)");

    List<CommandBuilder> commands = Arrays.asList(
      new MockCommand(noOptions.build()),
      new MockCommand(oneOption.build()),
      new MockCommand(severalOptions.build()),
      new MockCommand(withParameter.build()),
      new MockCommand(withNotes.build()),
      new CommandGroup(
        io,
        "cmds",
        "Some commands",
        new MockCommand(noOptions.build()),
        new MockCommand(oneOption.build())
      )
    );

    help = new Help(io, commands);
  }

  @Test(groups = "fast")
  public void testNoCommands() {
    Help onlyHelp = new Help(io, Collections.<CommandBuilder>emptyList());
    CliParser parser = new CliParser(onlyHelp.defineCommand(), Collections.<String>emptyList());

    onlyHelp.runCommand(parser);
    Assert.assertEquals(io.getOut(), "help <command_name>\n  Displays help for commands\n");
    Assert.assertEquals(io.getErr(), "");
  }

  @Test(groups = "fast")
  public void testCommandSummary() {
    CliParser parser = new CliParser(help.defineCommand(), Collections.<String>emptyList());

    help.runCommand(parser);
    Assert.assertEquals(
      io.getOut(),
      "foo\n" +
        "  My awesome foo command\n" +
        "bar\n" +
        "  My awesome bar command\n  (even more awesome than foo)\n" +
        "baz\n" +
        "  Meh\n" +
        "pram <cool>\n" +
        "  This has a param\n" +
        "noted\n" +
        "  I have notes\n" +
        "cmds\n" +
        "  Some commands\n" +
        "help <command_name>\n" +
        "  Displays help for commands\n"
    );
    Assert.assertEquals(io.getErr(), "");
  }

  @Test(groups = "fast")
  public void testNoOptions() {
    CliParser parser = new CliParser(help.defineCommand(), Arrays.asList("foo"));

    help.runCommand(parser);
    Assert.assertEquals(
      io.getOut(),
      "foo\n" +
        "  My awesome foo command\n"
    );
    Assert.assertEquals(io.getErr(), "");
  }

  @Test(groups = "fast")
  public void testOneOption() {
    CliParser parser = new CliParser(help.defineCommand(), Arrays.asList("bar"));

    help.runCommand(parser);
    Assert.assertEquals(
      io.getOut(),
      "bar\n" +
        "  My awesome bar command\n  (even more awesome than foo)\n" +
        "\n" +
        "  -b --bar <bar>\n" +
        "    [Required]\n"
    );
    Assert.assertEquals(io.getErr(), "");
  }

  @Test(groups = "fast")
  public void testSeveralOptions() {
    CliParser parser = new CliParser(help.defineCommand(), Arrays.asList("baz"));

    help.runCommand(parser);
    Assert.assertEquals(
      io.getOut(),
      "baz\n" +
        "  Meh\n" +
        "\n" +
        "  -x <hello>\n" +
        "    [Required] Make everything awesome\n" +
        "  -f\n" +
        "    [Optional] A flag of some sort\n" +
        "               that enables something\n" +
        "  -d --default <option>\n" +
        "    [Optional] Default\n" +
        "    default: testing\n"
    );
    Assert.assertEquals(io.getErr(), "");
  }

  @Test(groups = "fast")
  public void testWithParameter() {
    CliParser parser = new CliParser(help.defineCommand(), Arrays.asList("pram"));

    help.runCommand(parser);
    Assert.assertEquals(
      io.getOut(),
      "pram <cool>\n" +
        "  This has a param\n"
    );
    Assert.assertEquals(io.getErr(), "");
  }

  @Test(groups = "fast")
  public void testWithNotes() {
    CliParser parser = new CliParser(help.defineCommand(), Arrays.asList("noted"));

    help.runCommand(parser);
    Assert.assertEquals(
      io.getOut(),
      "noted\n" +
        "  I have notes\n" +
        "\n" +
        "  -b --bar <bar>\n" +
        "    [Required]\n" +
        "    e.g., hello\n" +
        "    e.g., goodbye\n" +
        "\n" +
        "  Yes, that's the same option\n" +
        "  (it's a good option)\n"
    );
    Assert.assertEquals(io.getErr(), "");
  }

  @Test(groups = "fast")
  public void testCommandGroupSummary() {
    CliParser parser = new CliParser(help.defineCommand(), Arrays.asList("cmds"));

    help.runCommand(parser);
    Assert.assertEquals(
      io.getOut(),
      "cmds foo\n" +
        "  My awesome foo command\n" +
        "cmds bar\n" +
        "  My awesome bar command\n  (even more awesome than foo)\n" +
        "cmds help <command_name>\n" +
        "  Displays help for commands\n"
    );
    Assert.assertEquals(io.getErr(), "");
  }

  @Test(groups = "fast")
  public void testCommandGroupHelp() {
    CliParser parser = new CliParser(help.defineCommand(), Arrays.asList("cmds", "bar"));

    help.runCommand(parser);
    Assert.assertEquals(
      io.getOut(),
      "bar\n" +
        "  My awesome bar command\n  (even more awesome than foo)\n" +
        "\n" +
        "  -b --bar <bar>\n" +
        "    [Required]\n"
    );
    Assert.assertEquals(io.getErr(), "");
  }

  private static class MockCommand implements CommandBuilder {
    private final CliCommand command;

    private MockCommand(CliCommand command) {
      this.command = command;
    }

    @Override
    public CliCommand defineCommand() {
      return command;
    }

    @Override
    public void runCommand(CliParser parser) {
    }
  }
}
