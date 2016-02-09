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

import com.facebook.tools.parser.CliParser;
import com.facebook.tools.io.IO;

import java.util.Arrays;
import java.util.List;

public class CommandRunner {
  private final IO io;
  private final CommandBuilder command;
  private final boolean printStackTraces;

  private CommandRunner(IO io, CommandBuilder command, boolean printStackTraces) {
    this.io = io;
    this.command = command;
    this.printStackTraces = printStackTraces;
  }

  public CommandRunner(IO io, CommandBuilder command) {
    this(io, command, false);
  }

  public static CommandRunner printStackTraces(IO io, CommandBuilder command) {
    return new CommandRunner(io, command, true);
  }

  public int run(String... arguments) {
    return run(Arrays.asList(arguments));
  }

  public int run(List<String> arguments) {
    CliParser parser = new CliParser(command.defineCommand(), arguments);

    return run(command, parser);
  }

  public int run(CommandBuilder command, CliParser parser) {
    try {
      parser.verify(io.out);

      if (command instanceof CommandGroup) {
        return ((CommandGroup) command).run(parser);
      } else {
        command.runCommand(parser);
      }
    } catch (ErrorMessage e) {
      io.err.println(e.getMessage());

      Throwable cause = e.getCause();

      while (cause != null) {
        io.err.printfln("(caused by: %s)", cause.getMessage());
        cause = cause.getCause();
      }

      if (printStackTraces) {
        e.printStackTrace(io.err);
      }

      return e.getErrorCode();
    } catch (Exception e) {
      io.err.printfln("An unexpected error occurred: %s", e.getMessage());

      Throwable cause = e.getCause();

      while (cause != null) {
        io.err.printfln("(caused by: %s)", cause.getMessage());
        cause = cause.getCause();
      }

      if (printStackTraces) {
        e.printStackTrace(io.err);
      }

      return -1;
    }

    return 0;
  }
}
