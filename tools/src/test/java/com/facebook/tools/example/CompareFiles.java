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
import com.facebook.tools.parser.CliCommand;
import com.facebook.tools.parser.CliParser;
import com.facebook.tools.subprocess.Subprocess;
import com.facebook.tools.io.IO;

import java.io.File;

public class CompareFiles implements CommandBuilder {
  private final IO io;

  public CompareFiles(IO io) {
    this.io = io;
  }

  @Override
  public CliCommand defineCommand() {
    CliCommand.Builder builder = new CliCommand.Builder(
      "compare-files",
      "Stupid example that compares files side-by-side by calling diff -y."
    );
    builder.addParameter("file1")
      .withDescription("Left-hand-side file")
      .withExample("/tmp/a.txt");
    builder.addParameter("file2")
      .withDescription("Right-hand-side file")
      .withExample("/tmp/b.txt");
    builder.addOption("--width")
      .withMetavar("columns")
      .withDefault("80")
      .withDescription("Number of columns to show");

    return builder.build();
  }

  @Override
  public void runCommand(CliParser parser) {
    compareFiles(
      parser.get("file1", Converters.FILE),
      parser.get("file2", Converters.FILE),
      parser.get("--width", Converters.INT)
    );
  }

  public void compareFiles(File leftHandSideFile, File rightHandSideFile, int width) {
    Subprocess diff = io.subprocess.forCommand("diff")
      .withArguments("-y")
      .withArguments("--width", width)
      .withArguments(leftHandSideFile, rightHandSideFile)
      .stream();

    for (String line : diff) {
      io.out.println(line);
    }

    if (diff.failed()) {
      io.err.println("Files differ");
    }
  }

  public static void main(String... args) {
    IO io = new IO();
    CommandRunner runner = new CommandRunner(io, new CompareFiles(io));

    System.exit(runner.run(args));
  }
}
