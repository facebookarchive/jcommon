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
package com.facebook.tools.subprocess;

import com.facebook.tools.io.IO;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds and starts an operating system process.  There are two modes a process can be in:
 * <dl>
 * <dt>streaming</dt>
 * <dd>
 * An unlimited amount of output is allowed, but the command may block if it is not consumed.
 * </dd>
 * <dt>non-streaming</dt>
 * <dd>
 * The command is guranteed to not block, but the amount of output is limited (e.g., first 10k).
 * </dd>
 * </dl>
 * Which is to say, if you want to run a quick command that you expect to produce a fixed amount of
 * output (e.g., "fbpackage info foo:123"), then you want to use non-streaming mode.  If you want to
 * run a command that can produce a lot of data, (e.g., "ptail foo") then you want to use streaming
 * mode.
 * <p/>
 * Streaming commands are started by calling {@link SubprocessBuilder.Builder#stream()}.
 * Non-streaming commands are started by calling {@link SubprocessBuilder.Builder#start()}.
 * A streaming command can be turned into a non-streaming command by calling
 * {@link Subprocess#background()}.
 * <p/>
 * Stderr is always in non-streaming mode.  This ensures you never have to worry about your command
 * blocking because you haven't read error output.  If you expect (and need to process) a lot of
 * output to stderr, then you need to {@link SubprocessBuilder.Builder#redirectStderrToStdout}.
 *
 * @see Subprocess
 */
public class SubprocessBuilder {
  private final ProcessBuilderWrapper builder;

  public SubprocessBuilder() {
    this.builder = new JavaProcessBuilderWrapper();
  }

  public SubprocessBuilder(ProcessBuilderWrapper builder) {
    this.builder = builder;
  }

  public Builder forCommand(String command) {
    return new Builder(command, builder);
  }

  public static class Builder {
    private final List<String> command = new ArrayList<>();
    private final Map<String, String> environmentOverrides = new LinkedHashMap<>();
    private final ProcessBuilderWrapper builder;

    private boolean redirectStderrToStdout = false;
    private File workingDirectory = null;
    private IO echoCommand = null;
    private IO echoOutput = null;
    private int outputBytesLimit = 512_000;

    private Builder(String command, ProcessBuilderWrapper builder) {
      this.command.add(command);
      this.builder = builder;
    }

    public Builder withArguments(List<?> arguments) {
      for (Object argument : arguments) {
        command.add(String.valueOf(argument));
      }

      return this;
    }

    public Builder withArguments(Object... arguments) {
      return withArguments(Arrays.asList(arguments));
    }

    public Builder withEnvironmentVariable(String key, String value) {
      environmentOverrides.put(key, value);

      return this;
    }

    public Builder withoutEnvironmentVariable(String key) {
      environmentOverrides.put(key, null);

      return this;
    }

    public Builder redirectStderrToStdout() {
      redirectStderrToStdout = true;

      return this;
    }

    public Builder withWorkingDirectory(File workingDirectory) {
      this.workingDirectory = workingDirectory;

      return this;
    }

    public Builder echoCommand(IO io) {
      echoCommand = io;

      return this;
    }

    public Builder echoOutput(IO io) {
      echoOutput = io;

      return this;
    }

    public Builder outputBytesLimit(int outputBytesLimit) {
      this.outputBytesLimit = outputBytesLimit;

      return this;
    }

    /**
     * Creates and begins running the command in non-streaming mode.
     *
     * @return a running command
     */
    public Subprocess start() {
      return start(false);
    }

    /**
     * Creates and begins running the command in streaming mode.
     *
     * @return a running command
     */
    public Subprocess stream() {
      return start(true);
    }

    private Subprocess start(boolean streaming) {
      final Process process;

      if (redirectStderrToStdout) {
        process = builder.createProcess(
          RedirectErrorsTo.STDOUT, environmentOverrides, workingDirectory, command
        );
      } else {
        process = builder.createProcess(
          RedirectErrorsTo.STDERR, environmentOverrides, workingDirectory, command
        );
      }

      if (echoCommand != null) {
        Iterator<String> iterator = command.iterator();

        while (iterator.hasNext()) {
          echoCommand.out.print(iterator.next());

          if (iterator.hasNext()) {
            echoCommand.out.print(' ');
          }
        }

        echoCommand.out.println();
      }

      return new SubprocessImpl(command, process, echoOutput, outputBytesLimit, streaming);
    }
  }
}
