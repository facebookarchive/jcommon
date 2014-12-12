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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.facebook.testing.ThreadHelper;

public class TestParallelRunner {

  private ParallelRunner parallelRunner;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    parallelRunner = new ParallelRunner(Executors.newFixedThreadPool(10), "test-");
  }

  @Test(groups = {"fast", "local"})
  public void testThreadName() throws Exception {
    final String threadNamePrefix = "sloth";
    final AtomicBoolean hasNextRef = new AtomicBoolean(true);
    final AtomicReference<String> errorMessage = new AtomicReference<>();
    final CountDownLatch latch = new CountDownLatch(1);
    final BlockingQueue<Runnable> taskQueue = new LinkedBlockingDeque<>();
    taskQueue.add(
      new Runnable() {
        @Override
        public void run() {
          String threadName = Thread.currentThread().getName();
          if (!threadName.contains(threadNamePrefix)) {
            errorMessage.set(
              String.format(
                "threadName mistmatch [%s] vs [%s]", threadNamePrefix, threadName
              )
            );
          }
          latch.countDown();
        }
      }
    );

    ThreadHelper threadHelper = new ThreadHelper();
    final Thread slothThread = threadHelper.doInThread(
      new Runnable() {
        @Override
        public void run() {

          parallelRunner.parallelRun(taskQueue, 2, threadNamePrefix);
        }
      }, "parallel-runner-sheppard"
    );

    latch.await();

    if (errorMessage.get() != null) {
      Assert.fail(errorMessage.get());
    }

    hasNextRef.set(false);
    slothThread.join();
  }
}
