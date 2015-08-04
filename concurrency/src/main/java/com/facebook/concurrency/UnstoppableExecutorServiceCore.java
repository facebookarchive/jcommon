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

import com.google.common.collect.Lists;
import org.joda.time.DateTimeUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.facebook.collections.ListMapper;
import com.facebook.collectionsbase.Mapper;

/**
 * core class that Unstoppable[Scheduled]ExecutorService delegates termination
 * methods to in order to guard shutdown
 */
class UnstoppableExecutorServiceCore {
  private final AtomicInteger remaining = new AtomicInteger(0);
  private final Object remainingTasksMapLock = new Object();
  private final Map<Runnable, Runnable> remainingTasksMap =
    Collections.synchronizedMap(new IdentityHashMap<>());
  private volatile boolean isShutdown = false;

  public List<Runnable> registerRunnableList(List<Runnable> taskList) {
    if (isShutdown) {
      throw new RejectedExecutionException("executor shutdown already");
    }

    List<Runnable> result = new ArrayList<>();

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

    List<TrackedCallable<V>> result = new ArrayList<>();

    for (Callable<V> task : taskList) {
      result.add(new TrackedCallableImpl<>(task));
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

    return new TrackedCallableImpl<>(task);
  }

  private void decrementRemaining() {
    if (remaining.decrementAndGet() == 0) {
      synchronized (remaining) {
        remaining.notifyAll();
      }
    }
  }

  private void addRunnable(Runnable runnable) {
    synchronized (remainingTasksMapLock) {
      remainingTasksMap.put(runnable, runnable);
    }
  }

  private void removeRunnable(Runnable runnable) {
    synchronized (remainingTasksMapLock) {
      remainingTasksMap.remove(runnable);
    }
  }

  private List<Runnable> drainRemainingTasksMap() {
    synchronized (remainingTasksMapLock) {
      List<Runnable> runnableList = Lists.newArrayList(remainingTasksMap.values());

      remainingTasksMap.clear();

      return runnableList;
    }
  }

  public synchronized void shutdown() {
    if (isShutdown) {
      return;
    }

    isShutdown = true;
  }

  public synchronized List<Runnable> shutdownNow() {
    if (isShutdown) {
      throw new IllegalStateException("already shutdown");
    }

    isShutdown = true;

    return drainRemainingTasksMap();
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
    return new TrackedFuture<>(future, task);
  }

  public <V> ScheduledFuture<V> trackScheduledFuture(
    ScheduledFuture<V> future, Completable task
  ) {
    return new TrackedScheduledFuture<>(future, task);
  }

  private class TrackedRunnableImpl implements TrackedRunnable {
    private final Runnable delegate;
    private final AtomicBoolean hasCompleted = new AtomicBoolean(false);

    private TrackedRunnableImpl(Runnable delegate) {
      this.delegate = delegate;
      remaining.incrementAndGet();
      addRunnable(delegate);
    }

    @Override
    public void run() {
      try {
        removeRunnable(delegate);
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
    private final Runnable runnable;

    private TrackedCallableImpl(Callable<V> delegate) {
      this.delegate = delegate;
      runnable = () -> {
        try {
          TrackedCallableImpl.this.delegate.call();
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      };
      remaining.incrementAndGet();
      addRunnable(runnable);
    }

    @Override
    public V call() throws Exception {
      try {
        removeRunnable(runnable);


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

  private static class TrackedFuture<V> extends WrappedFuture<V> {
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

  private static class TrackedScheduledFuture<V> extends WrappedScheduledFuture<V> {
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

  private static class FutureMapper<V> implements Mapper<Future<V>, Future<V>> {
    private final List<? extends Completable> completableList;
    private int index = 0;

    private FutureMapper(List<? extends Completable> completableList) {
      this.completableList = completableList;
    }

    @Override
    public Future<V> map(Future<V> input) {
      TrackedFuture<V> trackedFuture = new TrackedFuture<>(input, completableList.get(index));

      index++;

      return trackedFuture;
    }
  }
}
