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
