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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * same as UnstoppableExecutorService but for ScheduledExecutorService
 */
public class UnstoppableScheduledExecutorService
  implements ScheduledExecutorService {
  private static final Logger LOG = LoggerFactory.getLogger(UnstoppableScheduledExecutorService.class);

  private final UnstoppableExecutorServiceCore executorCore;
  private final ScheduledExecutorService executor;
  // guarded by: shutdownLock
  private final Map<ScheduledFuture<?>, ScheduledFuture<?>> 
    outstandingScheduledTasks = 
    Collections.synchronizedMap(new IdentityHashMap());
  // this lock is used to make sure that nothing can be put into
  // outstandingScheduledTasks after shutdown
  private final ReadWriteLock shutdownLock = new ReentrantReadWriteLock();
  
  public UnstoppableScheduledExecutorService(
    ScheduledExecutorService executor
  ) {
    this.executor = executor;
    executorCore = new UnstoppableExecutorServiceCore();
  }

  // helper functions that takes a Runnable/Callable and manages the necessary 
  // lifecycle so that scheduled tasks will
  //  1. be submitted
  //  2. have ScheduledFuture stored for canc ellation at shutdown

  private ScheduledFuture<?> internalScheduleRunnable(
    Runnable runnable, RunnableCallback callback
  ) {
    // task.openGate() below will add items to the outstandingScheduledTasks
    // array. Make sure nothing is added after shutdown
    shutdownLock.readLock().lock();

    try {
      // this will track if the task completes 
      TrackedRunnable trackedTask = executorCore.registerTask(runnable);
      BookkeepingTask task = new BookkeepingTask<Void>(trackedTask);
      // this will track if the task is cancelled
      ScheduledFuture<?> scheduledFuture = 
        executorCore.trackScheduledFuture(callback.submit(task), trackedTask);

      task.openGate(scheduledFuture);

      return scheduledFuture;
    } finally {
      shutdownLock.readLock().unlock();
    }
  }

  private <V> ScheduledFuture<V> internalScheduleCallable(
    Callable<V> callable, CallableCallback<V> callback
  ) {
    shutdownLock.readLock().lock();

    try {
      // same logic as above
      TrackedCallable<V> trackedTask = executorCore.registerTask(callable);
      BookkeepingTask task = new BookkeepingTask<V>(trackedTask);
      ScheduledFuture<V> scheduledFuture = 
        executorCore.trackScheduledFuture(callback.submit(task), trackedTask);

      task.openGate(scheduledFuture);

      return scheduledFuture;
    } finally {
      shutdownLock.readLock().unlock();
    }
  }

  // cancel any scheduled tasks
  private void cancelPendingTasks() {
    for (IdentityHashMap.Entry<ScheduledFuture<?>, ScheduledFuture<?>> entry :
      outstandingScheduledTasks.entrySet()
      ) {
      entry.getValue().cancel(false);
    }
  }

  @Override
  public ScheduledFuture<?> schedule(
    Runnable command, final long delay, final TimeUnit unit
  ) {
    return internalScheduleRunnable(command, new RunnableCallback() {
      @Override
      public ScheduledFuture<?> submit(Runnable task) {
        return executor.schedule(task, delay, unit);
      }
    });
  }

  @Override
  public <V> ScheduledFuture<V> schedule(
    Callable<V> callable, final long delay, final TimeUnit unit
  ) {
    return internalScheduleCallable(callable, new CallableCallback<V>() {
      @Override
      public ScheduledFuture<V> submit(Callable<V> task) {
        return executor.schedule(task, delay, unit);
      }
    });
  }

  @Override
  public ScheduledFuture<?> scheduleAtFixedRate(
    final Runnable command,
    final long initialDelay,
    final long period,
    final TimeUnit unit
  ) {
    return internalScheduleRunnable(command, new RunnableCallback() {
      @Override
      public ScheduledFuture<?> submit(Runnable task) {
        return executor.scheduleAtFixedRate(task, initialDelay, period, unit);
      }
    });
  }

  @Override
  public ScheduledFuture<?> scheduleWithFixedDelay(
    final Runnable command,
    final long initialDelay,
    final long delay,
    final TimeUnit unit
  ) {
    return internalScheduleRunnable(command, new RunnableCallback() {
      @Override
      public ScheduledFuture<?> submit(Runnable task) {
        return executor.scheduleWithFixedDelay(command, initialDelay, delay, unit);
      }
    }
    );
  }

  @Override
  public <T> Future<T> submit(Callable<T> task) {
    TrackedCallable<T> trackedTask = executorCore.registerTask(task);

    return executorCore.trackFuture(executor.submit(trackedTask), trackedTask);
  }

  @Override
  public <T> Future<T> submit(Runnable task, T result) {
    TrackedRunnable trackedTask = executorCore.registerTask(task);

    return executorCore.trackFuture(
      executor.submit(trackedTask, result), trackedTask
    );
  }

  @Override
  public Future<?> submit(Runnable task) {
    TrackedRunnable trackedTask = executorCore.registerTask(task);

    return executorCore.trackFuture(executor.submit(trackedTask), trackedTask);
  }

  @Override
  public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
    throws InterruptedException {
    List<TrackedCallable<T>> trackedTaskList = 
      executorCore.registerCallableList(tasks);

    return executorCore.trackFutureList(
      executor.invokeAll(trackedTaskList), trackedTaskList
    );
  }

  @Override
  public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
    throws InterruptedException {
    List<TrackedCallable<T>> trackedTaskList = 
      executorCore.registerCallableList(tasks);

    return executorCore.trackFutureList(
      executor.invokeAll(trackedTaskList, timeout, unit), trackedTaskList
    );
  }

  @Override
  public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
    throws InterruptedException, ExecutionException {
    
    List<TrackedCallable<T>> trackedTaskList = 
      executorCore.registerCallableList(tasks);

    return executor.invokeAny(trackedTaskList);
  }

  @Override
  public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
    throws InterruptedException, ExecutionException, TimeoutException {
    
    List<TrackedCallable<T>> trackedTaskList = 
      executorCore.registerCallableList(tasks);
    
    return executor.invokeAny(trackedTaskList, timeout, unit);
  }

  @Override
  public void execute(Runnable command) {
    executor.execute(executorCore.registerTask(command));
  }

  // shutdown/termination functions delegate to the UnstoppableExecutorCore

  @Override
  public void shutdown() {
    // the core uses the same impl here
    shutdownNow();
  }

  @Override
  public List<Runnable> shutdownNow() {
    // this blocks any internalSchedule*() calls until we shutdown the core.
    // it also blocks remove on outstandingScheduledTasks to avoid a 
    // ConcurrentModificationException
    shutdownLock.writeLock().lock();
    
    try {
    List<Runnable> runnableList = executorCore.shutdownNow();
    
    cancelPendingTasks();
    
    return runnableList;
    } finally {
      shutdownLock.writeLock().unlock();
    } 
  }

  @Override
  public boolean isShutdown() {
    return executorCore.isShutdown();
  }

  @Override
  public boolean isTerminated() {
    return executorCore.isTerminated();
  }

  @Override
  public boolean awaitTermination(long timeout, TimeUnit unit)
    throws InterruptedException {
    return executorCore.awaitTermination(timeout, unit);
  }

  /**
   * class that wraps a Runnable/Callable and handles :
   * 1. put()/remove() of the ScheduledFuture into a hash
   * 2. gating so that the task cannot be run until the future is in the hash
   * (avoid race condition that we remove() before we put())
   *
   * @param <V> : return result of the Callable
   */
  private class BookkeepingTask<V> implements Runnable, Callable<V> {
    private final CountDownLatch gate = new CountDownLatch(1);
    private final Callable<V> callable;
    private final Runnable runnable;
    private ScheduledFuture<?> future;

    private BookkeepingTask(Callable<V> callable) {
      this.runnable = null;
      this.callable = callable;
    }

    private BookkeepingTask(Runnable runnable) {
      this.runnable = runnable;
      this.callable = null;
    }

    /**
     * does the prologue for a task which also enables it to start
     *
     * @param future future for this task; will be stored in an IdentityHash
     */
    public void openGate(ScheduledFuture<V> future) {
      this.future = future;
      outstandingScheduledTasks.put(future, future);
      gate.countDown();
    }

    private V internalRun() throws Exception {
      try {
        gate.await();

        if (runnable != null) {
          runnable.run();

          return null;
        } else {
          return callable.call();
        }
      } catch (InterruptedException e) {
        LOG.info("interrupted, skipping execution of task");
        Thread.currentThread().interrupt();

        return null;
      } finally {
        cleanup();
      }
    }
    
    private void cleanup() {
      // we can't modify outstandingScheduledTasks while we're shutting down
      shutdownLock.readLock().lock();
      
      try {
        outstandingScheduledTasks.remove(future);        
      } finally {
        shutdownLock.readLock().unlock();        
      }
    }

    @Override
    public void run() {
      try {
        internalRun();
      } catch (Exception e) {
        LOG.error("exception during execution", e);
      }
    }


    @Override
    public V call() throws Exception {
      return internalRun();
    }
  }

  private interface RunnableCallback<V> {
    public ScheduledFuture<?> submit(Runnable task);
  }

  private interface CallableCallback<V> {
    public ScheduledFuture<V> submit(Callable<V> task);
  }
}
