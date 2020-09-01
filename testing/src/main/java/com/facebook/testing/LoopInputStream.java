/*
 * Copyright (C) 2012 Facebook, Inc.
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
package com.facebook.testing;

import com.facebook.collections.Factory;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class LoopInputStream extends InputStream {
  private final Factory<InputStream> streamFactory;
  private final AtomicBoolean stop = new AtomicBoolean(false);
  private final AtomicInteger loopCount = new AtomicInteger(0);
  private volatile InputStream inputStream;

  public LoopInputStream(Factory<InputStream> streamFactory) {
    this.streamFactory = streamFactory;
    nextLoop();
  }

  @Override
  public synchronized int read() throws IOException {
    if (stop.get()) {
      return -1;
    }

    int c;

    do {
      c = inputStream.read();

      if (c != -1) {
        return c;
      }

      nextLoop();

    } while (true);
  }

  private void nextLoop() {
    inputStream = streamFactory.create();

    synchronized (loopCount) {
      loopCount.incrementAndGet();
      loopCount.notifyAll();
    }
  }

  public void stop() {
    stop.set(true);
  }

  public int getLoopCount() {
    return loopCount.get();
  }

  public void waitForLoopCount(int count) throws InterruptedException {
    while (loopCount.get() < count) {
      synchronized (loopCount) {
        loopCount.wait(1000);
      }
    }
  }
}
