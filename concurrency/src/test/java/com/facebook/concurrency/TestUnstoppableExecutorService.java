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

import com.facebook.testing.Function;
import com.facebook.testing.MockExecutor;
import com.facebook.testing.TestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class TestUnstoppableExecutorService {
  private static final Logger LOG = LoggerFactory.getLogger(TestUnstoppableExecutorService.class);
  private static final Runnable NO_OP = new Runnable() {
    @Override
    public void run() {
    }
  };

  private ExecutorService executor;
  private MockExecutor mockExecutor;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    mockExecutor = new MockExecutor();
    executor = new UnstoppableExecutorService(mockExecutor);
  }

  @Test(groups = "fast")
  public void testShutdown() throws Exception {
    executor.shutdown();

    Assert.assertFalse(
      mockExecutor.isShutdown(), "mockExecutor should not be shutdown"
    );
    Assert.assertTrue(
      executor.isShutdown(), "executor should be shut down"
    );
  }

  @Test(groups = "fast")
  public void testShutdownNow() throws Exception {
    Assert.assertTrue(
      executor.shutdownNow().isEmpty(),
      "shutdownNow should return empty list"
    );

    Assert.assertFalse(
      mockExecutor.isShutdown(), "mockExecutor should not be shutdown"
    );
    Assert.assertTrue(
      executor.isShutdown(), "executor should be shut down"
    );
  }

  @Test(groups = "fast")
  public void testAwaitTermination() throws Exception {
    Assert.assertFalse(
      executor.awaitTermination(1, TimeUnit.NANOSECONDS),
      "executor is terminated"
    );

    executor.shutdown();
    mockExecutor.drain();

    Assert.assertTrue(
      executor.awaitTermination(1, TimeUnit.NANOSECONDS),
      "executor should be terminated"
    );

    Assert.assertTrue(
      executor.isTerminated(),
      "executor should be terminated"
    );
  }

  @Test(groups = "fast")
  public void testAwaitTerminationForExecute() throws Exception {
    Assert.assertFalse(
      executor.awaitTermination(1, TimeUnit.NANOSECONDS),
      "executor is terminated"
    );

    AtomicInteger completed = TestUtils.countCompletedRunnables(
      10,
      new Function<Runnable>() {
        @Override
        public void execute(Runnable argument) {
          executor.execute(argument);
        }
      }
    );

    executor.shutdown();
    mockExecutor.drain();

    Assert.assertTrue(
      executor.awaitTermination(1, TimeUnit.NANOSECONDS),
      "executor should be terminated"
    );

    Assert.assertEquals(completed.get(), 10);

    Assert.assertTrue(
      executor.isTerminated(),
      "executor should be terminated"
    );
  }

  @Test(groups = "fast")
  public void testAwaitTerminationForSubmitRunnable1() throws Exception {
    Assert.assertFalse(
      executor.awaitTermination(1, TimeUnit.NANOSECONDS),
      "executor is terminated"
    );

    AtomicInteger completed = TestUtils.countCompletedRunnables(
      10,
      new Function<Runnable>() {
        @Override
        public void execute(Runnable argument) {
          executor.submit(argument);
        }
      }
    );

    executor.shutdown();
    mockExecutor.drain();

    Assert.assertTrue(
      executor.awaitTermination(1, TimeUnit.NANOSECONDS),
      "executor should be terminated"
    );

    Assert.assertEquals(completed.get(), 10);

    Assert.assertTrue(
      executor.isTerminated(),
      "executor should be terminated"
    );
  }

  @Test(groups = "fast")
  public void testAwaitTerminationForSubmitRunnable2() throws Exception {
    Assert.assertFalse(
      executor.awaitTermination(1, TimeUnit.NANOSECONDS),
      "executor is terminated"
    );

    AtomicInteger completed = TestUtils.countCompletedRunnables(
      10,
      new Function<Runnable>() {
        @Override
        public void execute(Runnable argument) {
          executor.submit(argument, new Object());
        }
      }
    );

    executor.shutdown();
    mockExecutor.drain();

    Assert.assertTrue(
      executor.awaitTermination(1, TimeUnit.NANOSECONDS),
      "executor should be terminated"
    );

    Assert.assertEquals(completed.get(), 10);

    Assert.assertTrue(
      executor.isTerminated(),
      "executor should be terminated"
    );
  }

  @Test(groups = "fast")
  public void testAwaitTerminationForSubmitCallable() throws Exception {
    Assert.assertFalse(
      executor.awaitTermination(1, TimeUnit.NANOSECONDS),
      "executor is terminated"
    );

    AtomicInteger completed = TestUtils.<Void>countCompletedCallables(
      10,
      new Function<Callable<Void>>() {
        @Override
        public void execute(Callable<Void> argument) {
          executor.submit(argument);
        }
      }
    );

    executor.shutdown();
    mockExecutor.drain();

    Assert.assertEquals(completed.get(), 10);
    Assert.assertTrue(
      executor.awaitTermination(1, TimeUnit.NANOSECONDS),
      "executor should be terminated"
    );


    Assert.assertTrue(
      executor.isTerminated(),
      "executor should be terminated"
    );
  }

  @Test(groups = "fast")
  public void testTaskCompletesThenCancel() throws Exception {
    final AtomicReference<Future> future = new AtomicReference<Future>();
    AtomicInteger completed = TestUtils.<Void>countCompletedCallables(
      10,
      new Function<Callable<Void>>() {
        @Override
        public void execute(Callable<Void> argument) {
          future.compareAndSet(null, executor.submit(argument));
        }
      }
    );

    executor.shutdown();
    mockExecutor.drain();

    // this makes sure if we cancel an already completed task, it won't 
    // affect the awaitTermination check
    future.get().cancel(false);

    Assert.assertEquals(completed.get(), 10);
    Assert.assertTrue(
      executor.awaitTermination(1, TimeUnit.NANOSECONDS),
      "executor should be terminated"
    );
  }

  @Test(groups = "fast")
  public void testSubmission() throws Exception {
    executor.execute(NO_OP);
    Assert.assertEquals(mockExecutor.getNumPendingTasks(), 1);
    executor.submit(NO_OP);
    Assert.assertEquals(mockExecutor.getNumPendingTasks(), 2);
    executor.submit(NO_OP);
    Assert.assertEquals(mockExecutor.getNumPendingTasks(), 3);
    executor.submit(NO_OP, new Object());
    Assert.assertEquals(mockExecutor.getNumPendingTasks(), 4);
  }

  @Test(groups = "fast")
  public void testRejectedAfterShutdown() throws Exception {
    executor.shutdown();

    try {
      executor.submit(NO_OP);
      Assert.fail("expected exception");
    } catch (RejectedExecutionException e) {
      // success
      Assert.assertEquals(executor.isShutdown(), true);
      Assert.assertEquals(mockExecutor.isShutdown(), false);
    }
  }

  @Test(groups = "fast")
  public void testRate() throws Exception {
    int numTasks = 1000000;
    // use enough threads to induce lock contention
    int numThreads = 6;
    final AtomicInteger count = new AtomicInteger(0);
    ExecutorService realExecutor = Executors.newFixedThreadPool(numThreads);
    executor = new UnstoppableExecutorService(realExecutor);
    LatchTask blockedNoOp = new LatchTask(
      new Runnable() {
        @Override
        public void run() {
          count.incrementAndGet();
        }
      }
    );

    LOG.info("generating {} tasks", numTasks);

    for (int i = 0; i < numTasks; i++) {
      executor.submit(blockedNoOp);
    }

    executor.shutdown();
    LOG.info("starting tasks");
    long start = System.nanoTime();

    blockedNoOp.proceed();
    boolean terminated = executor.awaitTermination(5, TimeUnit.MINUTES);
    long end = System.nanoTime();

    Assert.assertTrue(terminated);
    Assert.assertEquals(count.get(), numTasks);

    double timeTakenMillis = (end - start) / (double) 1000000;
    // rate is primarily affected by rate that executor and get tasks to queues, ie pull from
    // the queue
    LOG.info(
      "%d tasks with %d threads took %f ms", numTasks, numThreads, timeTakenMillis
    );

    realExecutor.shutdown();
  }
}
