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

import com.facebook.tools.ErrorMessage;
import com.facebook.tools.io.IO;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

class SubprocessImpl implements Subprocess {
  private final List<String> command;
  private final Process process;
  private final ExecutorService stdoutExecutorService;
  private final ExecutorService stderrExecutorService;
  private final String name;
  private final Output stdout;
  private final Output stderr;
  private final Future<?> stdoutFuture;
  private final Future<?> stderrFuture;
  private final AtomicBoolean consumedStdout = new AtomicBoolean(false);
  private final Thread shutdownHook;

  SubprocessImpl(
    List<String> command, Process process, IO echo, int outputBytesLimit, boolean streaming
  ) {
    this.command = new ArrayList<>(command);
    this.process = process;

    StringBuilder name = new StringBuilder(80);
    Iterator<String> nameIterator = command.iterator();

    while (nameIterator.hasNext()) {
      name.append(nameIterator.next());

      if (nameIterator.hasNext()) {
        name.append(' ');
      }
    }

    this.name = name.toString();

    InputStream processInputStream = process.getInputStream();
    InputStream processErrorStream = process.getErrorStream();

    if (echo != null) {
      processInputStream = new EchoInputStream(processInputStream, echo.out);
      processErrorStream = new EchoInputStream(processErrorStream, echo.err);
    }

    stdout = new Output(processInputStream, outputBytesLimit, streaming);
    stderr = new Output(processErrorStream, outputBytesLimit, false);
    stdoutExecutorService =
      Executors.newCachedThreadPool(new NamedDaemonThreadFactory(name + "-stdout"));
    stderrExecutorService =
      Executors.newCachedThreadPool(new NamedDaemonThreadFactory(name + "-stderr"));
    stdoutFuture = stdoutExecutorService.submit(stdout);
    stderrFuture = stderrExecutorService.submit(stderr);

    shutdownHook = new Thread(
      new Runnable() {
        @Override
        public void run() {
          //noinspection EmptyTryBlock,UnusedDeclaration
          try (
            InputStream inputStream = process.getInputStream();
            OutputStream outputStream = process.getOutputStream();
            InputStream errorStream = process.getErrorStream()
          ) {
          } catch (IOException | RuntimeException ignored) {
          }

          process.destroy();
        }
      }
    );

    Runtime.getRuntime().addShutdownHook(shutdownHook);
  }


  @Override
  public List<String> command() {
    return command;
  }

  @Override
  public int waitFor() {
    stdout.background();

    try {
      int result = process.waitFor();

      stdoutFuture.get();
      stderrFuture.get();

      return result;
    } catch (InterruptedException e) {
      throw new ErrorMessage(e, "Interrupted while waiting for: %s", name);
    } catch (ExecutionException e) {
      throw new ErrorMessage(e, "Error while waiting for: %s", name);
    } finally {
      close();
    }
  }

  @Override
  public int waitFor(OutputStream out) {
    try {
      InputStream in = getStdOut();
      byte[] buffer = new byte[4096];
      int read;

      while ((read = in.read(buffer)) != -1) {
        out.write(buffer, 0, read);
      }
    } catch (IOException e) {
      throw new ErrorMessage(e, "Error while waiting for %s", name);
    }

    try {
      return process.waitFor();
    } catch (InterruptedException e) {
      throw new ErrorMessage(e, "Interrupted while waiting for %s", name);
    } finally {
      close();
    }
  }

  @Override
  public int waitFor(File outFile) {
    try (OutputStream out = new FileOutputStream(outFile)) {
      return waitFor(out);
    } catch (IOException e) {
      throw new ErrorMessage(e, "Error saving to %s", outFile);
    }
  }

  @Override
  public void kill() {
    //noinspection EmptyTryBlock,UnusedDeclaration
    try (
      Output stdout = this.stdout;
      Output stderr = this.stderr;
      InputStream inputStream = process.getInputStream();
      OutputStream outputStream = process.getOutputStream();
      InputStream errorStream = process.getErrorStream()
    ) {
    } catch (IOException | RuntimeException ignored) {
    }

    process.destroy();
    stdoutFuture.cancel(true);
    stderrFuture.cancel(true);
    stdoutExecutorService.shutdownNow();
    stderrExecutorService.shutdownNow();
    Runtime.getRuntime().removeShutdownHook(shutdownHook);
  }

  @Override
  public void close() {
    kill();
  }

  @Override
  public String getOutput() {
    waitFor();

    return new String(stdout.getContent(), StandardCharsets.UTF_8);
  }

  @Override
  public String getError() {
    waitFor();

    return new String(stderr.getContent(), StandardCharsets.UTF_8);
  }

  @Override
  public BufferedInputStream getStream() {
    return new BufferedInputStream(getStdOut());
  }

  @Override
  public BufferedReader getReader() {
    return new BufferedReader(new InputStreamReader(getStdOut(), StandardCharsets.UTF_8));
  }

  @Override
  public void background() {
    stdout.background();
  }

  @Override
  public void send(String content) {
    OutputStream outputStream = process.getOutputStream();

    try {
      outputStream.write(content.getBytes(StandardCharsets.UTF_8));
      outputStream.flush();
    } catch (IOException e) {
      throw new ErrorMessage(e, "Error while sending content to %s", name);
    }
  }

  @Override
  public int returnCode() {
    waitFor();

    return process.exitValue();
  }

  @Override
  public boolean succeeded() {
    return returnCode() == 0;
  }

  @Override
  public boolean failed() {
    return returnCode() != 0;
  }

  @Override
  public Iterator<String> iterator() {
    final BufferedReader reader = getReader();

    return new Iterator<String>() {
      private String line;
      private boolean pending = false;

      @Override
      public boolean hasNext() {
        if (pending) {
          return true;
        }

        try {
          line = reader.readLine();
        } catch (IOException e) {
          throw new ErrorMessage(e, "Error while running %s", name);
        }

        pending = true;

        return line != null;
      }

      @Override
      public String next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }

        pending = false;

        return line;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  @Override
  public String toString() {
    return "SubprocessImpl{" +
      "name='" + name + '\'' +
      ", consumedStdout=" + consumedStdout +
      '}';
  }

  private InputStream getStdOut() {
    if (!consumedStdout.compareAndSet(false, true)) {
      throw new IllegalStateException("Already consumed stdout: " + name);
    }

    return stdout;
  }

  private static class NamedDaemonThreadFactory implements ThreadFactory {
    private final String name;

    private NamedDaemonThreadFactory(String name) {
      this.name = name;
    }

    @Override
    public Thread newThread(Runnable task) {
      Thread thread = Executors.defaultThreadFactory().newThread(task);

      thread.setName(name);
      thread.setDaemon(true);

      return thread;
    }
  }

  private static class EchoInputStream extends FilterInputStream {
    private final OutputStream echo;

    private EchoInputStream(InputStream in, OutputStream echo) {
      super(in);
      this.echo = echo;
    }

    @Override
    public int read() throws IOException {
      int read = in.read();

      if (read != -1) {
        echo.write(read);
      }

      return read;
    }

    @Override
    public int read(byte[] result) throws IOException {
      int read = in.read(result);

      if (read != -1) {
        echo.write(result, 0, read);
      }

      return read;
    }

    @Override
    public int read(byte[] result, int offset, int length) throws IOException {
      int read = in.read(result, offset, length);

      if (read != -1) {
        echo.write(result, offset, read);
      }

      return read;
    }
  }
}
