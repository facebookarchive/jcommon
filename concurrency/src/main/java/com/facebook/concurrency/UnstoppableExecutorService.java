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

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * the idea here is that we want to shield an ExecutorService from any call that might shut it down,
 * while preserving the semantics that any user of this object can still shut it down, await
 * termination, etc from its perspective
 *
 * <p>Think of it as a localized view of another executor
 */
public class UnstoppableExecutorService implements ExecutorService {
  private final UnstoppableExecutorServiceCore executorCore;
  private final ExecutorService executor;

  public UnstoppableExecutorService(ExecutorService executor) {
    this.executor = executor;
    executorCore = new UnstoppableExecutorServiceCore();
  }

  @Override
  public <T> Future<T> submit(Callable<T> task) {
    return executor.submit(executorCore.registerTask(task));
  }

  @Override
  public <T> Future<T> submit(Runnable task, T result) {
    TrackedRunnable trackedTask = executorCore.registerTask(task);

    return executorCore.trackFuture(executor.submit(trackedTask, result), trackedTask);
  }

  @Override
  public Future<?> submit(Runnable task) {
    TrackedRunnable trackedTask = executorCore.registerTask(task);

    return executorCore.trackFuture(executor.submit(trackedTask), trackedTask);
  }

  @Override
  public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
      throws InterruptedException {

    List<TrackedCallable<T>> trackedTaskList = executorCore.registerCallableList(tasks);

    return executorCore.trackFutureList(executor.invokeAll(trackedTaskList), trackedTaskList);
  }

  @Override
  public <T> List<Future<T>> invokeAll(
      Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
      throws InterruptedException {

    List<TrackedCallable<T>> trackedTaskList = executorCore.registerCallableList(tasks);

    return executorCore.trackFutureList(
        executor.invokeAll(trackedTaskList, timeout, unit), trackedTaskList);
  }

  @Override
  public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
      throws InterruptedException, ExecutionException {

    List<TrackedCallable<T>> trackedTaskList = executorCore.registerCallableList(tasks);

    return executor.invokeAny(trackedTaskList);
  }

  @Override
  public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
      throws InterruptedException, ExecutionException, TimeoutException {

    List<TrackedCallable<T>> trackedTaskList = executorCore.registerCallableList(tasks);

    return executor.invokeAny(trackedTaskList, timeout, unit);
  }

  @Override
  public void execute(Runnable command) {
    executor.execute(executorCore.registerTask(command));
  }

  // shutdown/termination functions delegate to the UnstoppableExecutorCore
  @Override
  public void shutdown() {
    executorCore.shutdown();
  }

  @Override
  public List<Runnable> shutdownNow() {
    return executorCore.shutdownNow();
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
  public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
    return executorCore.awaitTermination(timeout, unit);
  }
}
