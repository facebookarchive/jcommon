package com.facebook.concurrency;

import java.util.concurrent.CountDownLatch;

public class LatchTask implements Runnable {
  private final CountDownLatch hasRunLatch = new CountDownLatch(1);
  private final CountDownLatch canRunLatch;

  private LatchTask(int value) {
    canRunLatch = new CountDownLatch(value);
  }
  
  public LatchTask() {
    this(0);
  }
  
  public static LatchTask createPaused() {
    return new LatchTask(1);
  }

  @Override
  public void run() {
    try {
      canRunLatch.await();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

    hasRunLatch.countDown();
  }

  /**
   * if paused, signals the task to proceed
   */
  public void proceed() {
    canRunLatch.countDown();
  }
  
  public void await() throws InterruptedException {
    hasRunLatch.await();
  }
}
