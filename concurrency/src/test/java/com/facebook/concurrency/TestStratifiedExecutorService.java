package com.facebook.concurrency;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.ExecutorService;

import com.facebook.collections.CounterMap;
import com.facebook.testing.LoopThread;
import com.facebook.testing.MockExecutor;
import com.facebook.testing.ThreadHelper;

public class TestStratifiedExecutorService {
  private StratifiedExecutorServiceBuilder builder;
  private CounterMap<String> runnableCountByTag;
  private LatchTask fuuLatch1;
  private TaggedRunnable fuuRunnable1;
  private LatchTask fuuLatch2;
  private TaggedRunnable fuuRunnable2;
  private LatchTask barLatch;
  private TaggedRunnable barRunnable;
  private LatchTask defaultRunnable;
  private MockExecutor mockExecutor;
  private LatchTask bazLatch;
  private TaggedRunnable bazRunnable;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    mockExecutor = new MockExecutor();

    ExecutorServiceFactory<ExecutorService> executorServiceFactory = () -> mockExecutor;

    builder = new StratifiedExecutorServiceBuilder(executorServiceFactory)
      .setMaxGlobalThreads(3)
      .setMaxThreadsPerTag(1);
    runnableCountByTag = new CounterMap<>();
    fuuLatch1 = LatchTask.createPaused(() -> runnableCountByTag.addAndGet("fuu", 1));
    fuuRunnable1 = new TaggedRunnable("fuu", fuuLatch1);
    fuuLatch2 = LatchTask.createPaused(() -> runnableCountByTag.addAndGet("fuu", 1));
    fuuRunnable2 = new TaggedRunnable("fuu", fuuLatch2);
    barLatch = LatchTask.createPaused(() -> runnableCountByTag.addAndGet("bar", 1));
    barRunnable = new TaggedRunnable("bar", barLatch);
    bazLatch = LatchTask.createPaused(() -> runnableCountByTag.addAndGet("baz", 1));
    bazRunnable = new TaggedRunnable("baz", bazLatch);
    defaultRunnable = LatchTask.createPaused(() -> runnableCountByTag.addAndGet("default", 1));
  }

  /**
   * tests that a limit of 1 task per tag is enforced
   *
   * @throws Exception
   */
  @Test(groups = "fast")
  public void testPerTagLimit() throws Exception {
    StratifiedExecutorService executor = builder.build();

    executor.execute(fuuRunnable1);
    executor.execute(fuuRunnable2);
    // two tasks submitted, but only one should be allowed through to the core executor
    Assert.assertEquals(mockExecutor.getNumPendingTasks(), 1);

    LoopThread loopThread = startMockExecutorDrainer();
    // the next sequence checks that fuu1 has entered the LatchTask, but fuu2 has not
    fuuLatch1.waitForStart();
    Assert.assertEquals(runnableCountByTag.get("fuu"), 0);
    Assert.assertEquals(fuuLatch2.hasStarted(), false);
    // allow fuu1 to start and consequently fuu2
    fuuLatch1.proceed();
    fuuLatch1.await();
    Assert.assertEquals(runnableCountByTag.get("fuu"), 1);
    // wait for fuu2
    fuuLatch2.proceed();
    fuuLatch2.await();
    loopThread.join();
    // and check count
    Assert.assertEquals(runnableCountByTag.get("fuu"), 2);
  }

  @Test(groups = "fast")
  public void testShutdownNoStart() throws Exception {
    StratifiedExecutorService executor = builder.build();

    executor.execute(fuuRunnable1);
    executor.execute(defaultRunnable);

    executor.shutdown();
    Assert.assertEquals(mockExecutor.getNumPendingTasks(), 2);
    Assert.assertFalse(fuuLatch1.hasStarted());
    Assert.assertFalse(defaultRunnable.hasStarted());
  }

  @Test
  public void testGlobalLimit() throws Exception {
    StratifiedExecutorService executor = builder.build();
    LoopThread loopThread1 = startMockExecutorDrainer();
    LoopThread loopThread2 = startMockExecutorDrainer();
    LoopThread loopThread3 = startMockExecutorDrainer();
    Assert.assertTrue(fuuLatch1.pauseCompletion());
    Assert.assertTrue(fuuLatch2.pauseCompletion());
    Assert.assertTrue(barLatch.pauseCompletion());
    executor.execute(fuuRunnable1);
    executor.execute(barRunnable);
    executor.execute(bazRunnable);
    executor.execute(defaultRunnable);
    fuuLatch1.proceed();
    barLatch.proceed();
    bazLatch.proceed();
    fuuLatch1.await();
    barLatch.await();
    bazLatch.await();
    Assert.assertEquals(runnableCountByTag.get("fuu"), 1);
    Assert.assertEquals(runnableCountByTag.get("bar"), 1);
    Assert.assertEquals(runnableCountByTag.get("baz"), 1);
    Assert.assertEquals(runnableCountByTag.get("default"), 0);
    fuuLatch1.resumeCompletion();
    barLatch.resumeCompletion();
    bazLatch.resumeCompletion();
    defaultRunnable.proceed();
    loopThread1.join();
    loopThread2.join();
    loopThread3.join();
  }

  @Test
  public void testShutdownOneStart() throws Exception {
    StratifiedExecutorService executor = builder.build();

    executor.execute(fuuRunnable1);
    executor.execute(defaultRunnable);
    LoopThread loopThread = startMockExecutorDrainer();
    fuuLatch1.waitForStart();
    fuuLatch1.proceed();
    executor.shutdown();
    loopThread.join();

    Assert.assertEquals(mockExecutor.getNumPendingTasks(), 1);
    Assert.assertFalse(fuuLatch1.hasStarted());
    Assert.assertFalse(defaultRunnable.hasStarted());
  }

  private LoopThread startMockExecutorDrainer() {
    ThreadHelper threadHelper = new ThreadHelper();

    return threadHelper.repeatInThread(() -> mockExecutor.drain(1));
  }
}
