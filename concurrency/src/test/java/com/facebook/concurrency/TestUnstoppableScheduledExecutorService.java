package com.facebook.concurrency;

import com.facebook.testing.Function;
import com.facebook.testing.MockExecutor;
import com.facebook.testing.TestUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ConcurrentModificationException;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class TestUnstoppableScheduledExecutorService {
  private static final Runnable NO_OP = new Runnable() {
    @Override
    public void run() {
    }
  };

  private ScheduledExecutorService executor;
  private MockExecutor mockExecutor;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    mockExecutor = new MockExecutor();
    executor = new UnstoppableScheduledExecutorService(mockExecutor);
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
  public void testAwaitTermination1() throws Exception {
    ScheduledFuture<?> future = executor.schedule(NO_OP, 10, TimeUnit.SECONDS);

    Assert.assertFalse(
      executor.awaitTermination(1, TimeUnit.NANOSECONDS),
      "executor is terminated"
    );

    executor.shutdown();

    Assert.assertTrue(future.isCancelled(), "scheduled task should be cancelled");
  }
  
  @Test(groups = "fast")
  public void testAwaitTermination2() throws Exception {
  	Assert.assertFalse(
      executor.awaitTermination(1, TimeUnit.NANOSECONDS),
      "executor is terminated"
    );

    AtomicInteger completed = TestUtil.countCompletedRunnables(
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
  public void testScheduledTasksCancelledOnShutdown() throws Exception {
    ScheduledFuture<?> future1 = executor.schedule(NO_OP, 10, TimeUnit.SECONDS);
    ScheduledFuture<?> future2 =
      executor.schedule(Executors.callable(NO_OP), 10, TimeUnit.SECONDS);
    ScheduledFuture<?> future3 =
      executor.scheduleAtFixedRate(NO_OP, 10, 10, TimeUnit.SECONDS);
    ScheduledFuture<?> future4 =
      executor.scheduleWithFixedDelay(NO_OP, 10, 10, TimeUnit.SECONDS);

    Assert.assertFalse(
      executor.awaitTermination(1, TimeUnit.NANOSECONDS),
      "executor is terminated"
    );

    executor.shutdown();

    Assert.assertTrue(
      future1.isCancelled(), "scheduled task1 should be cancelled"
    );
    Assert.assertTrue(
      future2.isCancelled(), "scheduled task2 should be cancelled"
    );
    Assert.assertTrue(
      future3.isCancelled(), "scheduled task3 should be cancelled"
    );
    Assert.assertTrue(
      future4.isCancelled(), "scheduled task4 should be cancelled"
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
  public void testShutdownWhileExecuting() throws Exception {
    // a bit hackish this test provokes a race condition: if a scheduled()
    // task completes while shutdown() is removing pending tasks futures,
    // we could get a ConcurrentModificationException (without the fix
    // to do locking anyway)
    final int numShutdownTasks = 1000;
    int numExecutionThreads = 10;
    
    final AtomicInteger count = new AtomicInteger(0);
    final AtomicBoolean fail = new AtomicBoolean(false);
    Runnable shutdownTask = new Runnable() {
      @Override
      public void run() {
        try {
          // 10 is totally arbitrary here, but it seems to work
          if (count.incrementAndGet() == (int)((float)numShutdownTasks * .75)) {
            executor.shutdown();
          }
        } catch (ConcurrentModificationException e) {
          fail.set(true);
        }
      }
    };

    // schedule a ton of tasks that all shutdown the executor (fills up
    // its underlying hash)
    for (int i = 0; i < numShutdownTasks; i++) {
      executor.schedule(shutdownTask, 1, TimeUnit.MILLISECONDS);
    }

    // task that just removes the head of the MockExecutor, runs it,
    // and schedules it again
    Runnable executorTask = new Runnable() {
      @Override
      public void run() {
        while (true) {
          Runnable head;

          synchronized (this) {
            if (mockExecutor.getNumPendingTasks() > 0) {
              head = mockExecutor.removeHead();
            } else {
              return;
            }
          }

          head.run();
          try {
            executor.schedule(head, 1, TimeUnit.MILLISECONDS);
          } catch (RejectedExecutionException e) {
            // expected
          }
        }
      }
    };
    
    // number of execution threads to use for draining the executor
    Thread[] executorThreads = new Thread[10];
    for (int i = 0; i < numExecutionThreads; i++) {
      executorThreads[i] = new Thread(executorTask);
      executorThreads[i].start();
    }
    
    for (int i = 0; i < numExecutionThreads; i++) {
      executorThreads[i].join();      
    }
    
    Assert.assertFalse(
      fail.get(), "got concurrent modification exception during shutdown"
    );
  }
}
