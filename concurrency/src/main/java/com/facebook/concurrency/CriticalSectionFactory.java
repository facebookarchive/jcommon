package com.facebook.concurrency;

import java.util.concurrent.atomic.AtomicBoolean;

public class CriticalSectionFactory {
  private final AtomicBoolean isRunning = new AtomicBoolean(false);

  public Runnable wrap(final Runnable runnable) {
    return new Runnable() {
      @Override
      public void run() {
        if (isRunning.compareAndSet(false, true)) {
          try {
            runnable.run();
          }
          finally {
            isRunning.set(false);
          }
        }
      }
    };
  }
}
