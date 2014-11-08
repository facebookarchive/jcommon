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

import com.facebook.tools.io.MockIO;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class TestSubprocessBuilder {
  private Queue<CreateProcessParameters> parameters;
  private MockIO io;

  @BeforeMethod(alwaysRun = true)
  public void setUp() {
    parameters = new ArrayDeque<>();

    SubprocessBuilder builder = new SubprocessBuilder(
      new ProcessBuilderWrapper() {
        @Override
        public Process createProcess(
          RedirectErrorsTo redirectErrorsTo,
          Map<String, String> environmentOverrides,
          File workingDirectory,
          List<String> command
        ) {
          parameters.add(
            new CreateProcessParameters(
              redirectErrorsTo, environmentOverrides, workingDirectory, command
            )
          );

          return new DummyProcess();
        }
      }
    );

    io = new MockIO(builder);
  }

  @Test(groups = "fast")
  public void testStart() {
    io.subprocess.forCommand("foo").start();
    assertCommand("foo");
  }

  @Test(groups = "fast")
  public void testStream() {
    io.subprocess.forCommand("foo").stream();
    assertCommand("foo");
  }

  @Test(groups = "fast")
  public void testArguments() {
    io.subprocess.forCommand("foo")
      .withArguments("bar")
      .withArguments("this", "is", "a test")
      .withArguments(123)
      .withArguments(Arrays.asList(456, "abc"))
      .start();
    assertCommand("foo", "bar", "this", "is", "a test", "123", "456", "abc");
  }

  @Test(groups = "fast")
  public void testRedirectStdOutToStdErr() {
    io.subprocess.forCommand("foo").redirectStderrToStdout().start();
    assertCommand(RedirectErrorsTo.STDOUT, Collections.<String, String>emptyMap(), null, "foo");
  }

  @Test(groups = "fast")
  public void testEnvironmentVariables() {
    io.subprocess.forCommand("foo")
      .withEnvironmentVariable("bar", "baz")
      .withoutEnvironmentVariable("bad")
      .start();

    Map<String, String> expected = new LinkedHashMap<>();

    expected.put("bar", "baz");
    expected.put("bad", null);
    assertCommand(RedirectErrorsTo.STDERR, expected, null, "foo");
  }

  @Test(groups = "fast")
  public void testWorkingDirectory() {
    io.subprocess.forCommand("foo")
      .withWorkingDirectory(new File("/tmp/test"))
      .start();

    assertCommand(
      RedirectErrorsTo.STDERR, Collections.<String, String>emptyMap(), new File("/tmp/test"), "foo"
    );
  }

  @Test(groups = "fast")
  public void testEchoCommand() {
    io.subprocess.forCommand("foo")
      .withArguments("bar", "baz")
      .echoCommand(io)
      .start()
      .waitFor();

    assertCommand("foo", "bar", "baz");
    Assert.assertEquals(io.getOut(), "foo bar baz\n");
    Assert.assertEquals(io.getErr(), "");
  }

  @Test(groups = "fast")
  public void testEchoOutput() {
    io.subprocess.forCommand("foo")
      .echoOutput(io)
      .start()
      .waitFor();

    assertCommand("foo");
    Assert.assertEquals(io.getOut(), "out: this is a test of stdout\n");
    Assert.assertEquals(io.getErr(), "err: this is a test of stderr\n");
  }

  @Test(groups = "fast")
  public void testOutputBytesLimit() {
    Subprocess foo = io.subprocess.forCommand("foo")
      .echoOutput(io)
      .outputBytesLimit(19)
      .start();

    assertCommand("foo");
    Assert.assertEquals(foo.getOutput(), "out: this is a test");
    Assert.assertEquals(foo.getError(), "err: this is a test");
  }

  private void assertCommand(String... command) {
    assertCommand(RedirectErrorsTo.STDERR, Collections.<String, String>emptyMap(), null, command);
  }

  private void assertCommand(
    RedirectErrorsTo redirectErrorsTo,
    Map<String, String> environmentOverrides,
    File workingDirectory,
    String... command
  ) {
    CreateProcessParameters actual = parameters.poll();

    actual.assertParameters(redirectErrorsTo, environmentOverrides, workingDirectory, command);
  }

  private static class CreateProcessParameters {
    private final RedirectErrorsTo redirectErrorsTo;
    private final Map<String, String> environmentOverrides;
    private final File workingDirectory;
    private final List<String> command;

    private CreateProcessParameters(
      RedirectErrorsTo redirectErrorsTo,
      Map<String, String> environmentOverrides,
      File workingDirectory,
      List<String> command
    ) {
      this.redirectErrorsTo = redirectErrorsTo;
      this.environmentOverrides = environmentOverrides;
      this.workingDirectory = workingDirectory;
      this.command = command;
    }

    public void assertParameters(
      RedirectErrorsTo redirectErrorsTo,
      Map<String, String> environmentOverrides,
      File workingDirectory,
      String... command
    ) {
      Assert.assertEquals(this.command, Arrays.asList(command));
      Assert.assertEquals(this.redirectErrorsTo, redirectErrorsTo);
      Assert.assertEquals(this.workingDirectory, workingDirectory);
      Assert.assertEquals(this.environmentOverrides, environmentOverrides);
    }
  }

  private static class DummyProcess extends Process {
    private final LatchInputStream stdout = new LatchInputStream("out: this is a test of stdout\n");
    private final LatchInputStream stderr = new LatchInputStream("err: this is a test of stderr\n");

    @Override
    public OutputStream getOutputStream() {
      return null;
    }

    @Override
    public InputStream getInputStream() {
      return stdout;
    }

    @Override
    public InputStream getErrorStream() {
      return stderr;
    }

    @Override
    public int waitFor() {
      stdout.await();
      stderr.await();

      return 0;
    }

    @Override
    public int exitValue() {
      return 0;
    }

    @Override
    public void destroy() {
    }
  }

  private static class LatchInputStream extends InputStream {
    private final CountDownLatch eof = new CountDownLatch(1);
    private final InputStream delegate;

    private LatchInputStream(String content) {
      delegate = new ByteArrayInputStream(content.getBytes());
    }

    @Override
    public int read() throws IOException {
      int result = delegate.read();

      if (result == -1) {
        eof.countDown();
      }

      return result;
    }

    public void await() {
      try {
        eof.await(10, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        Assert.fail("Interrupted waiting for eof");
      }
    }
  }
}
