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
  private InputStream inputStream;


  public LoopInputStream(
    Factory<InputStream> streamFactory
  ) {
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
