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
package com.facebook.tools.example;

import com.facebook.tools.CommandBuilder;
import com.facebook.tools.CommandRunner;
import com.facebook.tools.ErrorMessage;
import com.facebook.tools.parser.CliCommand;
import com.facebook.tools.parser.CliParser;
import com.facebook.tools.subprocess.Subprocess;
import com.facebook.tools.io.IO;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class CreateReplayFile implements CommandBuilder {
  private final IO io;

  public CreateReplayFile(IO io) {
    this.io = io;
  }

  @Override
  public CliCommand defineCommand() {
    CliCommand.Builder command = new CliCommand.Builder(
      "create-replay-file",
      "Dumps output from ptail into a file, adding a fake checkpoint at the end."
    );

    command.addOption("--ptail")
      .withMetavar("path")
      .withDescription("Path to ptail command")
      .withDefault("/usr/local/bin/ptail");
    command.addOption("-n", "--lines")
      .withMetavar("lines")
      .withDescription("Number of lines to tail before stopping")
      .withExample("100000")
      .withDefault("100000");
    command.addParameter("input_category")
      .withDescription("Scribe category to tail")
      .withExample("page_requests");
    command.addParameter("ouput_dir")
      .withDescription("Path to save replay data to (will be overwritten)");

    return command.build();
  }

  @Override
  public void runCommand(CliParser parser) {
    createReplayFile(
      parser.get("--ptail"),
      parser.get("input_category"),
      parser.get("--lines", Converters.INT),
      new File(parser.get("output_dir"))
    );
  }

  public File createReplayFile(String ptailCommand, String category, int lines, File outputFile) {
    io.out.printfln("Saving %,d ptail lines to %s", lines, outputFile);

    Subprocess ptail = io.subprocess.forCommand(ptailCommand)
      .withArguments(category)
      .stream();
    int count = 0;

    try (Writer out = new BufferedWriter(new FileWriter(outputFile))) {
      long updateTime = System.currentTimeMillis();

      for (String line : ptail) {
        out.write(line);
        out.write('\n');
        ++count;

        if (count >= lines) {
          break;
        }

        if (System.currentTimeMillis() - updateTime > 1000) {
          updateTime = System.currentTimeMillis();
          io.out.statusf("%,d of %,d (%.01f%%)", count, lines, 100.0 * count / lines);
        }
      }

      if (count != lines) {
        throw new ErrorMessage("Ptail failed: %s", ptail.getError());
      }

      // write a fake checkpoint
      out.write("# cp=dummy\n");
      io.out.statusf("%,d of %,d (%.01f%%)", count, lines, 100.0 * count / lines);
    } catch (IOException e) {
      throw new ErrorMessage(e, "Error ptailing %s", category);
    }

    ptail.kill();

    return outputFile;
  }

  public static void main(String... args) {
    IO io = new IO();
    CommandRunner runner = new CommandRunner(io, new CreateReplayFile(io));

    System.exit(runner.run(args));
  }
}
