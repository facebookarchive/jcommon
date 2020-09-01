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

import com.facebook.testing.MockExecutor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestTaskGroup {
  private ExecutorService executor1;
  private ExecutorService executor2;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    executor1 = Executors.newSingleThreadExecutor();
    executor2 = Executors.newSingleThreadExecutor();
  }

  @AfterMethod(alwaysRun = true)
  public void tearDown() throws Exception {
    executor1.shutdownNow();
    executor2.shutdownNow();
  }

  @Test(groups = "fast")
  public void testSanity() throws Exception {
    MockExecutor mockExecutor = new MockExecutor();
    TaskGroup taskGroup = new TaskGroup(mockExecutor);
    // Register some tasks
    DelayRunnable runnable1 = new DelayRunnable();
    DelayRunnable runnable2 = new DelayRunnable();
    DelayRunnable runnable3 = new DelayRunnable();

    taskGroup.register(runnable1);
    taskGroup.register(runnable2);
    taskGroup.register(runnable3);

    TaskGroup.FinishLatch latch = taskGroup.execute();

    // Run all tasks
    mockExecutor.drain();

    // Should not block
    latch.await();

    Assert.assertTrue(runnable1.isRun());
    Assert.assertTrue(runnable2.isRun());
    Assert.assertTrue(runnable3.isRun());
  }

  @Test(groups = "fast")
  public void testSingleExecutor() throws Exception {
    TaskGroup taskGroup = new TaskGroup(executor1);

    DelayRunnable runnable1 = new DelayRunnable(500);
    DelayRunnable runnable2 = new DelayRunnable(0);

    taskGroup.register(runnable1);
    taskGroup.register(runnable2);

    taskGroup.execute().await();

    // Regardless of delay, all tasks should be run
    Assert.assertTrue(runnable1.isRun());
    Assert.assertTrue(runnable2.isRun());
  }

  @Test(groups = "fast")
  public void testMultipleExecutor() throws Exception {
    TaskGroup taskGroup = new TaskGroup();

    DelayRunnable runnable1 = new DelayRunnable(500);
    DelayRunnable runnable2 = new DelayRunnable(0);
    DelayRunnable runnable3 = new DelayRunnable(100);

    taskGroup.register(executor1, runnable1);
    taskGroup.register(executor2, runnable2);
    taskGroup.register(executor2, runnable3);

    taskGroup.execute().await();

    // Regardless of delay, all tasks should be run
    Assert.assertTrue(runnable1.isRun());
    Assert.assertTrue(runnable2.isRun());
    Assert.assertTrue(runnable3.isRun());
  }

  private static class DelayRunnable implements Runnable {
    private volatile boolean isRun = false;

    private final long delayMillis;

    private DelayRunnable(long delayMillis) {
      this.delayMillis = delayMillis;
    }

    private DelayRunnable() {
      this(0);
    }

    @Override
    public void run() {
      try {
        TimeUnit.MILLISECONDS.sleep(delayMillis);
      } catch (InterruptedException e) {
        // Do nothing
      }
      isRun = true;
    }

    public boolean isRun() {
      return isRun;
    }
  }
}
