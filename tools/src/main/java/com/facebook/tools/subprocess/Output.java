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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.CountDownLatch;

class Output extends InputStream implements Runnable {
  private static final int BUFFER_SIZE = 102400;

  private final InputStream inputStream;
  private final int outputBytesLimit;
  private final boolean streaming;
  private final PipedInputStream pipedIn = new PipedInputStream(BUFFER_SIZE);
  private final PipedOutputStream pipedOut = new PipedOutputStream();
  private final ByteArrayOutputStream content;
  private final CountDownLatch connected = new CountDownLatch(1);

  private volatile boolean background = false;

  Output(InputStream inputStream, int outputBytesLimit, boolean streaming) {
    this.inputStream = inputStream;
    this.outputBytesLimit = outputBytesLimit;
    this.streaming = streaming;
    content = new ByteArrayOutputStream(outputBytesLimit);
  }

  @Override
  public void run() {
    try (PipedOutputStream pipedOut = this.pipedOut; InputStream inputStream = this.inputStream) {
      byte[] buffer = new byte[BUFFER_SIZE];
      int read;

      if (streaming) {
        pipedOut.connect(pipedIn);
        connected.countDown();
      }

      while ((read = inputStream.read(buffer)) != -1) {
        if (streaming && !background) {
          pipedOut.write(buffer, 0, read);
          pipedOut.flush();
        }

        synchronized (content) {
          if (content.size() < outputBytesLimit) {
            content.write(buffer, 0, Math.min(read, outputBytesLimit - content.size()));
          }
        }
      }
    } catch (IOException | RuntimeException ignored) {
    }
  }

  public void background() {
    this.background = true;

    if (streaming) {
      try {
        pipedOut.close();
      } catch (IOException | RuntimeException ignored) {
      }
    }
  }

  public byte[] getContent() {
    synchronized (content) {
      return content.toByteArray();
    }
  }

  @Override
  public int read(byte[] buffer) throws IOException {
    waitForPipe();

    try {
      return pipedIn.read(buffer);
    } catch (IOException e) {
      if (background) {
        return -1;
      }

      throw e;
    }
  }

  @Override
  public int read(byte[] buffer, int offset, int length) throws IOException {
    waitForPipe();

    try {
      return pipedIn.read(buffer, offset, length);
    } catch (IOException e) {
      if (background) {
        return -1;
      }

      throw e;
    }
  }

  @Override
  public int read() throws IOException {
    waitForPipe();

    try {
      return pipedIn.read();
    } catch (IOException e) {
      if (background) {
        return -1;
      }

      throw e;
    }
  }

  @Override
  public void close() throws IOException {
    inputStream.close();
    pipedOut.close();
    pipedIn.close();
    super.close();
  }

  private void waitForPipe() {
    if (!streaming) {
      throw new IllegalStateException("Subprocess was not created for streaming");
    }

    try {
      connected.await();
    } catch (InterruptedException e) {
      // reset interrupt state and return
      Thread.currentThread().interrupt();
    }
  }
}
