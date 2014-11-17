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
import com.facebook.tools.parser.CliParameter;
import com.facebook.tools.parser.CliParser;
import com.facebook.tools.io.IO;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Help implements CommandBuilder {
  private final IO io;
  private final String group;
  // HACK create a delegate so that we can add "ourselves" to the commands list
  private final CommandBuilder delegate;
  private final List<CommandBuilder> commands;

  public Help(IO io, String group, List<CommandBuilder> commands) {
    this.io = io;
    this.group = group;
    this.delegate = new CommandBuilder() {
      @Override
      public CliCommand defineCommand() {
        CliCommand.Builder builder = new CliCommand.Builder("help", "Displays help for commands");

        builder.addParameter("command_name")
          .withDescription("Command to display help for")
          .withDefault(null);
        builder.allowTrailingParameters();

        return builder.build();
      }

      @Override
      public void runCommand(CliParser parser) {
        help(parser.get("command_name"), parser.getTrailing());
      }
    };
    this.commands = new ArrayList<>(commands);
    this.commands.add(delegate);
  }

  public Help(IO io, List<CommandBuilder> commands) {
    this(io, null, commands);
  }

  @Override
  public CliCommand defineCommand() {
    return delegate.defineCommand();
  }

  @Override
  public void runCommand(CliParser parser) {
    delegate.runCommand(parser);
  }

  public void help(String commandName, List<String> arguments) {
    List<CliCommand> cliCommands = new ArrayList<>(commands.size() + 1);
    CommandBuilder selectedCommand = null;

    for (CommandBuilder command : commands) {
      CliCommand cliCommand = command.defineCommand();

      cliCommands.add(cliCommand);

      if (cliCommand.getName().equals(commandName)) {
        selectedCommand = command;
      }
    }

    if (commandName == null) {
      printSummary(io.out, cliCommands);
    } else if (selectedCommand instanceof CommandGroup) {
      ((CommandGroup) selectedCommand).help(arguments);
    } else if (selectedCommand != null) {
      io.out.println(selectedCommand.defineCommand().getDocumentation());
    } else {
      printSummary(io.out, cliCommands);
      io.out.println();
      io.out.flush();

      throw new ErrorMessage("Unknown commandName: %s", commandName);
    }
  }

  private void printSummary(PrintStream out, List<CliCommand> cliCommands) {
    for (CliCommand command : cliCommands) {
      if (group != null) {
        out.print(group);
        out.print(' ');
      }

      out.print(command.getName());

      List<CliParameter> parameters = command.getParameters();

      if (!parameters.isEmpty()) {
        out.print(" <");

        Iterator<CliParameter> parameterIterator = parameters.iterator();

        while (parameterIterator.hasNext()) {
          out.print(parameterIterator.next().getName());

          if (parameterIterator.hasNext()) {
            out.print("> <");
          }
        }

        out.print('>');
      }

      out.println();

      for (String line : command.getDescription()) {
        out.print("  ");
        out.println(line);
      }
    }
  }
}
