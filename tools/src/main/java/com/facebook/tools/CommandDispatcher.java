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

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CommandDispatcher {
  private final Map<String, CommandBuilder> commandBuilderMap;
  private final Help help;
  private final CliCommand dispatchCommand;
  private final IO io;

  public CommandDispatcher(IO io, String group, List<CommandBuilder> commands) {
    this.io = io;
    commandBuilderMap = new LinkedHashMap<>();

    for (CommandBuilder builder : commands) {
      CliCommand command = builder.defineCommand();

      commandBuilderMap.put(command.getName(), builder);
    }

    help = new Help(io, group, commands);
    commandBuilderMap.put(help.defineCommand().getName(), help);

    CliCommand.Builder dispatchCommand = new CliCommand.Builder("dispatch", "Dispatches commands");

    dispatchCommand.addParameter("command").withDefault(null);
    dispatchCommand.allowTrailingParameters();
    this.dispatchCommand = dispatchCommand.build();
  }

  public CommandDispatcher(IO io, List<CommandBuilder> commands) {
    this(io, null, commands);
  }

  public CommandDispatcher(IO io, CommandBuilder... commands) {
    this(io, Arrays.asList(commands));
  }

  public int run(String... arguments) {
    return run(Arrays.asList(arguments));
  }

  public int run(List<String> arguments) {
    CliParser dispatchParser = new CliParser(dispatchCommand, arguments);
    String commandName = dispatchParser.get("command");
    boolean printStackTraces = "-X".equals(commandName);

    if (printStackTraces) {
      arguments = dispatchParser.getTrailing();
      dispatchParser = new CliParser(dispatchCommand, arguments);
      commandName = dispatchParser.get("command");
    }

    List<String> commandArguments = dispatchParser.getTrailing();
    CommandBuilder selectedCommand = commandBuilderMap.get(commandName);

    if (selectedCommand == null) {
      selectedCommand = help;
      commandArguments = arguments;
    }

    CommandRunner commandRunner;

    if (printStackTraces) {
      commandRunner = CommandRunner.printStackTraces(io, selectedCommand);
    } else {
      commandRunner = new CommandRunner(io, selectedCommand);
    }

    return commandRunner.run(commandArguments);
  }

  public void help(List<String> arguments) {
    CommandRunner helpCommandRunner = new CommandRunner(io, help);

    helpCommandRunner.run(arguments);
  }
}
