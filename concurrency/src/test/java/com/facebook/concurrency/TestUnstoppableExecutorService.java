package com.facebook.concurrency;

import com.facebook.testing.MockExecutor;
import com.facebook.testing.TestUtil;
import com.facebook.testing.Function;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class TestUnstoppableExecutorService {
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

    AtomicInteger completed = TestUtil.countCompletedRunnables(10,
      new Function<Runnable>() {
        @Override
        public void execute(Runnable argument) {
          executor.execute(argument);
        }
      });

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

    AtomicInteger completed = TestUtil.countCompletedRunnables(10,
      new Function<Runnable>() {
        @Override
        public void execute(Runnable argument) {
          executor.submit(argument);
        }
      });

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

    AtomicInteger completed = TestUtil.countCompletedRunnables(10,
      new Function<Runnable>() {
        @Override
        public void execute(Runnable argument) {
          executor.submit(argument, new Object());
        }
      });

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

    AtomicInteger completed = TestUtil.<Void>countCompletedCallables(10,
      new Function<Callable<Void>>() {
        @Override
        public void execute(Callable<Void> argument) {
          executor.submit(argument);
        }
      });

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
    AtomicInteger completed = TestUtil.<Void>countCompletedCallables(
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
}
