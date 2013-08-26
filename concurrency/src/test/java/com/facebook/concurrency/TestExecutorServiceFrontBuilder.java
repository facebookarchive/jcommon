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

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public class TestExecutorServiceFrontBuilder {
  private ExecutorService coreExecutor;
  private ExecutorServiceFrontBuilder executorFrontBuilder;
  private AtomicLong count;
  private CountDownLatch finishLatch;
  private CountDownLatch hangLatch;
  private CountDownLatch countLatch;
  private Runnable hangTask;
  private Runnable countTask;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    count = new AtomicLong(0);
    finishLatch = new CountDownLatch(4);
    countLatch = new CountDownLatch(2);
    hangLatch = new CountDownLatch(1);

    hangTask = new Runnable() {
      @Override
      public void run() {
        try {
          hangLatch.await();
          count.incrementAndGet();
          finishLatch.countDown();
        } catch (InterruptedException e) {
          throw new RuntimeException("interrupted waiting on latch!", e);
        }
      }
    };
    countTask = new Runnable() {
      @Override
      public void run() {
        count.incrementAndGet();
        countLatch.countDown();
        finishLatch.countDown();
      }
    };

    coreExecutor = Executors.newCachedThreadPool();
    executorFrontBuilder = new ExecutorServiceFrontBuilder(coreExecutor, 3);
  }

  @AfterMethod(alwaysRun = true)
  public void tearDown() throws Exception {
    coreExecutor.shutdown();
  }

  /**
   * This test will submit several tasks from two ExecutorServiceFront which
   * share threads on a global ExecutorServiceFront. Two of these tasks will
   * hang at a latch. Other tasks should completes on the third thread in
   * the global ExecutorServiceFront.
   */
  @Test(groups = "fast")
  public void testGlobalMax() throws Exception {
    ExecutorServiceFront executorFront1 =
      executorFrontBuilder
        .setMaxInstanceThreads(2)
        .build();

    ExecutorServiceFront executorFront2 =
      executorFrontBuilder
        .setMaxInstanceThreads(2)
        .build();

    /* submit 2 tasks that will hang */
    executorFront1.execute(hangTask);
    executorFront2.execute(hangTask);

    /* submit 2 new tasks */
    executorFront1.execute(countTask);
    executorFront2.execute(countTask);

    try {
      /* wait for all counter tasks to complete */
      countLatch.await();
      Assert.assertEquals(count.get(), 2);

      /* let the hang tasks proceed */
      hangLatch.countDown();

      /* wait for all tasks to complete */
      finishLatch.await();
      Assert.assertEquals(count.get(), 4);
    } catch (InterruptedException e) {
      throw new RuntimeException("interrupted waiting on latch!", e);
    }
  }
}
