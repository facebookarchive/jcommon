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
import java.util.List;

public class CommandGroup implements CommandBuilder {
  private final CliCommand command;
  private final CommandDispatcher commandDispatcher;

  public CommandGroup(IO io, String name, String description, List<CommandBuilder> commands) {
    CliCommand.Builder builder = new CliCommand.Builder(name, description)
      .allowTrailingParameters();

    command = builder.build();
    commandDispatcher = new CommandDispatcher(io, name, commands);
  }

  public void help(List<String> arguments) {
    commandDispatcher.help(arguments);
  }

  public CommandGroup(IO io, String name, String description, CommandBuilder... commands) {
    this(io, name, description, Arrays.asList(commands));
  }

  @Override
  public CliCommand defineCommand() {
    return command;
  }

  @Override
  public void runCommand(CliParser parser) {
    run(parser);
  }

  public int run(CliParser parser) {
    List<String> arguments = parser.getTrailing();

    return commandDispatcher.run(arguments);
  }
}
