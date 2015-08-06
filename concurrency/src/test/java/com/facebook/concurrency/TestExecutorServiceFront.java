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

import org.joda.time.DateTimeUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.facebook.testing.AnnotatedRunnable;
import com.facebook.testing.LoopThread;
import com.facebook.testing.MockExecutor;
import com.facebook.testing.TestUtils;
import com.facebook.testing.ThreadHelper;

public class TestExecutorServiceFront {
  private MockExecutor mockExecutor;
  private ExecutorServiceFront executorFront;
  private ExecutorServiceFront executorFront2;
  private AtomicLong count;
  private AtomicLong offsetTime;
  private Runnable countTask;
  private LatchTask latchTask;
  private Runnable slowTask;
  private static final int NUM_THREADS = 10;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    count = new AtomicLong(0);
    offsetTime = new AtomicLong(0);
    countTask = count::incrementAndGet;
    slowTask = () -> {
      try {
        DateTimeUtils.setCurrentMillisOffset(
          offsetTime.addAndGet(20000)
        );
      } catch (SecurityException e) {
        throw new RuntimeException("security exception on incrementing the system time!", e);
      }
      count.incrementAndGet();
    };
    latchTask = LatchTask.createPaused();
    mockExecutor = new MockExecutor();
    executorFront = new ExecutorServiceFront(
      mockExecutor, 10000, TimeUnit.MILLISECONDS
    );
    executorFront2 = new ExecutorServiceFront(
      new LinkedBlockingQueue<>(),
      mockExecutor,
      "fuu",
      2,
      10000,
      TimeUnit.MILLISECONDS
    );
  }

  /**
   * tests
   * 1. multiple submits result in only 1 task being submitted to underlying
   * executor
   * 2. running that one task drains our own queue
   */
  @Test(groups = "fast")
  public void testMaxDrainer() throws Exception {
    Assert.assertEquals(mockExecutor.getNumPendingTasks(), 0);
    executorFront.execute(countTask);
    Assert.assertEquals(mockExecutor.getNumPendingTasks(), 1);
    executorFront.execute(countTask);
    Assert.assertEquals(mockExecutor.getNumPendingTasks(), 1);
    mockExecutor.drain();
    Assert.assertEquals(mockExecutor.getNumPendingTasks(), 0);
    Assert.assertEquals(count.get(), 2);

  }

  /**
   * This test will submit several tasks at least one of which will
   * be the latch-task.  Another thread will try to drain the backing
   * executor.  This will hang that thread running a task
   */
  @Test(groups = "fast")
  public void testConcurrentDrainerAndSubmit() throws Exception {
    // thread will drain the executor backing us
    Thread drainingThread = new Thread(
      mockExecutor::drain
    );

    // submit a task that will hang, and a count task => 1 drainer task
    executorFront.execute(latchTask);
    executorFront.execute(countTask);
    Assert.assertEquals(mockExecutor.getNumPendingTasks(), 1);

    // hang at the latch task
    drainingThread.start();
    Assert.assertEquals(count.get(), 0);

    // submit a new task that will no result in a new drainer task
    executorFront.execute(countTask);

    // let the drainer proceed, and wait for it to complete
    latchTask.proceed();
    drainingThread.join();

    // should have 2 count tasks 
    Assert.assertEquals(count.get(), 2);

    // 0 drainer tasks, add a count task => 1 drainer task
    Assert.assertEquals(mockExecutor.getNumPendingTasks(), 0);
    executorFront.execute(countTask);
    Assert.assertEquals(mockExecutor.getNumPendingTasks(), 1);
  }

  /**
   * This test:
   * 1. submit several slow tasks all of which will expire. Check these tasks
   * are executed one by one by several drainers.
   * 2. submit several fast tasks all of which will not expire. Check all these
   * tasks are executed in batch by one drainer.
   */
  @Test(groups = "fast")
  public void testExpiringSingleDrainer() throws Exception {
    int numTask = 3;

    // submit several slow tasks
    for (int i = 0; i < numTask; i++) {
      executorFront.execute(slowTask);
    }
    Assert.assertEquals(mockExecutor.getNumPendingTasks(), 1);

    for (int i = 0; i < numTask; i++) {
      // a drainer should run one task each time, then expire 
      mockExecutor.removeHead().run();
      Assert.assertEquals(count.get(), i + 1);
    }

    //reset the counter
    count.set(0);

    // submit several fast task
    for (int i = 0; i < numTask; i++) {
      executorFront.execute(countTask);
    }
    Assert.assertEquals(mockExecutor.getNumPendingTasks(), 1);

    // a drainer should run all tasks
    mockExecutor.removeHead().run();
    Assert.assertEquals(count.get(), numTask);

    // must clear the timer offset
    DateTimeUtils.setCurrentMillisOffset(0);
  }

  /**
   * This test will submit several tasks two of which will
   * expire. We check the drainer(s) in the pending list
   * before and after the expiration.
   */
  @Test(groups = "fast")
  public void testExpiringDualDrainer() throws Exception {
    // submit three tasks
    executorFront2.execute(slowTask);
    executorFront2.execute(countTask);
    executorFront2.execute(slowTask);
    Assert.assertEquals(mockExecutor.getNumPendingTasks(), 2);
    AnnotatedRunnable drainer2 = mockExecutor.getRunnableList().get(1);

    mockExecutor.removeHead().run();

    // Drainer1 expires after the 1st task completes, 
    // and is rescheduled to the end of the pending list.
    Assert.assertEquals(mockExecutor.getNumPendingTasks(), 2);

    // Drainer2 should be on the head of the pending list    
    Assert.assertSame(drainer2, mockExecutor.getRunnableList().get(0));

    mockExecutor.removeHead().run();

    // Drainer2 expires after the 2nd and 3rd task complete.     
    AnnotatedRunnable drainer1 = mockExecutor.getRunnableList().get(0);

    // Drainer1 should be on the head of the pending list
    Assert.assertNotSame(drainer1, drainer2);

    // should have 2 slow tasks and 1 count tasks 
    Assert.assertEquals(count.get(), 3);

    // must clear the timer offset
    DateTimeUtils.setCurrentMillisOffset(0);
  }

  @DataProvider(name = "getTestExecutors")
  public Object[][] getTestExecutors() {
    return new Object[][]{
      {Executors.newFixedThreadPool(NUM_THREADS)},
      {new ExecutorServiceFront(
        new LinkedBlockingQueue<>(),
        Executors.newFixedThreadPool(NUM_THREADS),
        "fuu",
        NUM_THREADS, 1, TimeUnit.MILLISECONDS
      )}
    };
  }

  /**
   * Tests that tasks that die with RuntimeException don't cause the
   * Executor to lose threads.
   *
   * @param executor The test executor
   */
  @Test(groups = "fast", dataProvider = "getTestExecutors")
  public void testDyingThreads(Executor executor) throws Exception {
    final int numTasks = NUM_THREADS * 2;
    final CountDownLatch latch = new CountDownLatch(numTasks);
    // kill all the threads
    for (int i = 0; i < numTasks; i++) {
      executor.execute(
        () -> {
          latch.countDown();
          throw new RuntimeException("Expected Failure");
        }
      );
    }
    Assert.assertTrue(latch.await(5, TimeUnit.SECONDS));
    // now add more tasks to run
    final CountDownLatch latch2 = new CountDownLatch(NUM_THREADS);
    // Run tasks to see if they can still run
    for (int i = 0; i < NUM_THREADS; i++) {
      executor.execute(
        latch2::countDown
      );
    }
    Assert.assertTrue(latch.await(5, TimeUnit.SECONDS));
  }

  @Test(groups = "fast")
  public void testTimeExpirationWithEmptyQueue() throws Exception {
    try {
      LinkedBlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>();
      final ExecutorServiceFront executorServiceFront = new ExecutorServiceFront(
        workQueue, mockExecutor, "fuu", 1, 1, TimeUnit.SECONDS
      );
      DateTimeUtils.setCurrentMillisFixed(0);
      executorServiceFront.execute(latchTask);
      Assert.assertEquals(mockExecutor.getNumPendingTasks(), 1);

      Thread t = TestUtils.runInThread(
        () -> {
          AnnotatedRunnable drainer = mockExecutor.removeHead();

          drainer.run();
        },
        "drainer"
      );
      // once the queue is empty, we know the drainer has taken the task out and is blocked on the 
      // latch
      while (!workQueue.isEmpty()) {
        Thread.sleep(50);
      }
      // move time forward, which will cause the Drainer to terminate based on time-slice (not
      // empty queue)
      DateTimeUtils.setCurrentMillisFixed(1001);
      latchTask.proceed();
      // wait for drainer to terminate
      t.join();
      // our time has expired, and there are no tasks in the queue, no tasks should be in the queue  
      Assert.assertEquals(mockExecutor.getNumPendingTasks(), 0);
    } finally {
      DateTimeUtils.setCurrentMillisSystem();
    }
  }

  @Test(groups = "fast")
  public void testRenameThread() throws Exception {
    LinkedBlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>();
    final ExecutorServiceFront executorServiceFront = new ExecutorServiceFront(
      workQueue, mockExecutor, "custom-name", 1
    );
    executorServiceFront.execute(latchTask);
    Assert.assertEquals(mockExecutor.getNumPendingTasks(), 1);

    Thread t = createDrainerThread();
    // once the queue is empty, we know the drainer has taken the task out and is blocked on the
    // latch
    while (!workQueue.isEmpty()) {
      Thread.sleep(50);
    }
    
    Assert.assertEquals(t.getName(), "custom-name-000");
    latchTask.proceed();
    t.join();
    Assert.assertEquals(t.getName(), "original");
  }

  @Test(groups = "fast")
  public void testCreateManyThreads() throws Exception {
    LinkedBlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>();
    ExecutorServiceFront executorServiceFront = new ExecutorServiceFront(
      workQueue, mockExecutor, "custom-name", 2
    );
    LatchTask task1 = LatchTask.createPaused();
    LatchTask task2 = LatchTask.createPaused();
    // put 2 tasks  in the queue, both will hang, causing 2 drainers to be created
    executorServiceFront.execute(task1);
    executorServiceFront.execute(task2);

    ThreadHelper threadHelper = new ThreadHelper();
    // 1. submit two tasks that will each block at their start
    // 2. wait until they start
    // 3. check the thread name is changed accordingly
    // 4. let the tasks complete
    // 5. verify thread names are set back
    LoopThread drainerThread1 = createDrainerThread(threadHelper);
    task1.waitForStart();
    LoopThread drainerThread2 = createDrainerThread(threadHelper);
    task2.waitForStart();
    while (!workQueue.isEmpty()) {
      Thread.sleep(50);
    }

    // check that the names change according to our ESF
    Assert.assertEquals(drainerThread1.getName(), "custom-name-000");
    task1.proceed();
    task1.await();
    Assert.assertEquals(drainerThread2.getName(), "custom-name-001");
    task2.proceed();
    task2.await();
    drainerThread1.join();
    drainerThread2.join();
    // and that the 'base name' of the borrowed  threads is restored
    Assert.assertEquals(drainerThread1.getName(), "drainer");
    Assert.assertEquals(drainerThread2.getName(), "drainer");
  }

  private LoopThread createDrainerThread(ThreadHelper threadHelper) {
    return threadHelper.repeatInThread(
      () -> {
        AnnotatedRunnable drainer = mockExecutor.removeHead();
        if (drainer != null) {
          drainer.run();
        } else {
          try {
            Thread.sleep(10);
          } catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
        }
      },
      "drainer"
    );
  }

  private Thread createDrainerThread() {
    return TestUtils.runInThread(
      () -> {
        AnnotatedRunnable drainer = mockExecutor.removeHead();

        drainer.run();
      },
      "original"
    );
  }
}
