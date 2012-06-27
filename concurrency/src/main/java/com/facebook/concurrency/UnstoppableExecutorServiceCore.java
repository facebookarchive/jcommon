package com.facebook.concurrency;

import com.facebook.collections.ListMapper;
import com.facebook.collections.Mapper;
import org.joda.time.DateTimeUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * core class that Unstoppable[Scheduled]ExecutorService delegates termination
 * methods to in order to guard shutdown
 */
class UnstoppableExecutorServiceCore {
  private final AtomicInteger remaining = new AtomicInteger(0);
  private volatile boolean isShutdown = false;

  public List<Runnable> registerRunnableList(List<Runnable> taskList) {
    if (isShutdown) {
      throw new RejectedExecutionException("executor shutdown already");
    }

    List<Runnable> result = new ArrayList<Runnable>();

    for (Runnable task : taskList) {
      result.add(new TrackedRunnableImpl(task));
    }

    return result;
  }

  public <V> List<TrackedCallable<V>> registerCallableList(
    Collection<? extends Callable<V>> taskList
  ) {
    if (isShutdown()) {
      throw new RejectedExecutionException("executor shutdown already");
    }

    List<TrackedCallable<V>> result = new ArrayList<TrackedCallable<V>>();

    for (Callable<V> task : taskList) {
      result.add(new TrackedCallableImpl<V>(task));
    }

    return result;
  }

  public TrackedRunnable registerTask(Runnable task) {
    if (isShutdown()) {
      throw new RejectedExecutionException("executor shutdown already");
    }

    return new TrackedRunnableImpl(task);
  }

  public <V> TrackedCallable<V> registerTask(final Callable<V> task) {
    if (isShutdown()) {
      throw new RejectedExecutionException("executor shutdown already");
    }

    return new TrackedCallableImpl<V>(task);
  }

  private void decrementRemaining() {
    if (remaining.decrementAndGet() == 0) {
      synchronized (remaining) {
        remaining.notifyAll();
      }
    }
  }

  public synchronized void shutdown() {
    if (isShutdown) {
      return;
    }

    isShutdown = true;
  }

  public List<Runnable> shutdownNow() {
    // for now, shutdownNow() is equivalent to shutdown()
    shutdown();

    // TODO: we can track started tasks and actually interrupt them

    return Collections.emptyList();
  }

  public boolean isShutdown() {
    return isShutdown;
  }

  public boolean isTerminated() {
    assert remaining.get() >= 0;

    return remaining.get() == 0;
  }

  public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
    if (!isShutdown) {
      return false;
    }

    long start = DateTimeUtils.currentTimeMillis();

    synchronized (remaining) {
      while (remaining.get() > 0) {
        // timed wait due to likely lost notifications, so relatively short also
        remaining.wait(50);

        long elapsedMillis = DateTimeUtils.currentTimeMillis() - start;

        if (elapsedMillis > unit.toMillis(timeout)) {
          return false;
        }
      }
    }

    return true;
  }

  public <V> List<Future<V>> trackFutureList(
    List<Future<V>> futureList, List<? extends Completable> completableList
  ) {
    return ListMapper.map(futureList, new FutureMapper<V>(completableList));
  }

  public <V> Future<V> trackFuture(Future<V> future, Completable task) {
    return new TrackedFuture<V>(future, task);
  }

  public <V> ScheduledFuture<V> trackScheduledFuture(
    ScheduledFuture<V> future, Completable task
  ) {
    return new TrackedScheduledFuture<V>(future, task);
  }

  private class TrackedRunnableImpl implements TrackedRunnable {
    private final Runnable delegate;
    private final AtomicBoolean hasCompleted = new AtomicBoolean(false);

    private TrackedRunnableImpl(Runnable delegate) {
      this.delegate = delegate;
      remaining.incrementAndGet();
    }

    @Override
    public void run() {
      try {
        delegate.run();
      } finally {
        complete();
      }
    }

    public void complete() {
      if (hasCompleted.compareAndSet(false, true)) {
        decrementRemaining();
      }
    }
  }

  private class TrackedCallableImpl<V> implements TrackedCallable<V> {

    private final Callable<V> delegate;
    private final AtomicBoolean hasCompleted = new AtomicBoolean(false);

    private TrackedCallableImpl(Callable<V> delegate) {
      this.delegate = delegate;
      remaining.incrementAndGet();
    }

    @Override
    public V call() throws Exception {
      try {
        return delegate.call();
      } finally {
        complete();
      }
    }

    public void complete() {
      if (hasCompleted.compareAndSet(false, true)) {
        decrementRemaining();
      }
    }
  }

  private class TrackedFuture<V> extends WrappedFuture<V> {
    private final Completable task;

    private TrackedFuture(Future<V> delegate, Completable task) {
      super(delegate);
      this.task = task;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
      task.complete();

      return super.cancel(mayInterruptIfRunning);
    }
  }

  private class TrackedScheduledFuture<V> extends WrappedScheduledFuture<V> {
    private final Completable task;

    private TrackedScheduledFuture(
      ScheduledFuture<V> delegate, Completable task
    ) {
      super(delegate);
      this.task = task;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
      task.complete();

      return super.cancel(mayInterruptIfRunning);
    }
  }

  private class FutureMapper<V> implements Mapper<Future<V>, Future<V>> {
    private final List<? extends Completable> completableList;
    private int index = 0;

    private FutureMapper(List<? extends Completable> completableList) {
      this.completableList = completableList;
    }

    @Override
    public Future<V> map(Future<V> input) {
      TrackedFuture<V> trackedFuture = new TrackedFuture<V>(input, completableList.get(index));

      index++;

      return trackedFuture;
    }
  }
}
