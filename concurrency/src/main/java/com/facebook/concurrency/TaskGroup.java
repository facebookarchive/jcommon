package com.facebook.concurrency;

import com.facebook.collections.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Provides a grouping of tasks so that when executed, the caller will have
 * a latch to wait on to indicate when all registered tasks have completed.
 * Tasks may be registered with the default executor, or with another executor.
 */
public class TaskGroup {
  private final ExecutorService defaultExecutor;
  private final Collection<Pair<ExecutorService, Runnable>> taskPairs =
    new ArrayList<Pair<ExecutorService, Runnable>>();

  public TaskGroup(ExecutorService defaultExecutor) {
    this.defaultExecutor = defaultExecutor;
  }

  public TaskGroup() {
    this(null);
  }

  public synchronized void register(
    ExecutorService executorService, Runnable task
  ) {
    taskPairs.add(new Pair<ExecutorService, Runnable>(executorService, task));
  }

  public void register(Runnable task) {
    if (defaultExecutor == null) {
      throw new IllegalStateException("No default executor specified");
    }
    register(defaultExecutor, task);
  }

  public synchronized FinishLatch execute() {
    final CountDownLatch finishLatch =
      new CountDownLatch(taskPairs.size());
    for (final Pair<ExecutorService, Runnable> taskPair : taskPairs) {
      taskPair.getFirst().execute(new Runnable() {
        @Override
        public void run() {
          try {
            taskPair.getSecond().run();
          } finally {
            finishLatch.countDown();
          }
        }
      });
    }
    return new FinishLatch(finishLatch);
  }

  public static class FinishLatch {
    private final CountDownLatch finishLatch;

    private FinishLatch(CountDownLatch finishLatch) {
      this.finishLatch = finishLatch;
    }

    public boolean await(long waitTime, TimeUnit waitTimeUnit)
      throws InterruptedException {
      return finishLatch.await(waitTime, waitTimeUnit);
    }

    public void await() throws InterruptedException {
      finishLatch.await();
    }
  }
}
