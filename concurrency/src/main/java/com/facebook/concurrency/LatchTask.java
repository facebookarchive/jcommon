package com.facebook.concurrency;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class LatchTask implements Runnable {
  private final CountDownLatch hasRunLatch = new CountDownLatch(1);
  private final CountDownLatch canRunLatch;
  private final Runnable task;

  private LatchTask(int value, Runnable task) {
    canRunLatch = new CountDownLatch(value);
    this.task = task;
  }
  
  public LatchTask(Runnable work) {
    this(0, work);
  }

  public LatchTask() {
    this(0, NoOp.INSTANCE);
  }

  public static LatchTask createPaused() {
    return new LatchTask(1, NoOp.INSTANCE);
  }

  public static LatchTask createPaused(Runnable task) {
    return new LatchTask(1, task);
  }

  @Override
  public void run() {
    try {
      canRunLatch.await();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

    task.run();
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

  /**
   * @see CountDownLatch
   */
  public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
    return hasRunLatch.await(timeout, unit);
  }
}
