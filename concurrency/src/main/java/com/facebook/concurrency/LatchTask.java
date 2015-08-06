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
package com.facebook.concurrency;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class LatchTask implements Runnable {
  private final CountDownLatch startedLatch = new CountDownLatch(1);
  private final CountDownLatch hasRunLatch = new CountDownLatch(1);
  private final CountDownLatch canRunLatch;
  private final Semaphore canComplete = new Semaphore(1);
  private final Runnable task;

  private LatchTask(boolean canRun, Runnable task) {
    this.canRunLatch = new CountDownLatch(canRun ? 0 : 1); // 0 => latch won't block
    this.task = task;
  }

  public LatchTask(Runnable work) {
    this(true, work);
  }

  public LatchTask() {
    this(true, NoOp.INSTANCE);
  }

  public static LatchTask createPaused() {
    return new LatchTask(false, NoOp.INSTANCE);
  }

  public static LatchTask createPaused(Runnable task) {
    return new LatchTask(false, task);
  }

  @Override
  public void run() {
    try {
      startedLatch.countDown();
      canRunLatch.await();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

    task.run();
    hasRunLatch.countDown();

    try {
      canComplete.acquire(1);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } finally {
      canComplete.release(1);
    }
  }

  public synchronized void pauseAfterCompletion() {
    canComplete.drainPermits();
  }

  public synchronized LatchTask resumeAfterCompletion() {
    canComplete.release(1);

    return this;
  }

  /**
   * if paused, signals the task to proceed
   */
  public LatchTask proceed() {
    canRunLatch.countDown();

    return this;
  }

  public void waitForStart() throws InterruptedException {
    startedLatch.await();
  }

  public boolean hasStarted() {
    return startedLatch.getCount() == 0;
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
