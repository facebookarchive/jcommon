package com.facebook.concurrency;


import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.facebook.collectionsbase.Function;

public class StratifiedExecutorService extends AbstractExecutorService implements ExecutorService {
  private final ConcurrentCache<String, ExecutorService, RuntimeException> executorServiceCache;
  private final ExecutorService defaultExecutor;

  StratifiedExecutorService(
    final ExecutorServiceFactory<ExecutorService> executorServiceFactory, ExecutorService defaultExecutor
  ) {
    this.defaultExecutor = defaultExecutor;

    ValueFactory<String, ExecutorService, RuntimeException> valueFactory =
      input -> executorServiceFactory.create();

    executorServiceCache = new CoreConcurrentCache<>(valueFactory, RuntimeExceptionHandler.INSTANCE);
  }

  @Override
  public void shutdown() {
    for (Entry<String, CallableSnapshot<ExecutorService, RuntimeException>> entry : executorServiceCache) {
      entry.getValue().get();
    }
  }

  @Override
  public List<Runnable> shutdownNow() {
    final List<Runnable> remaining = new ArrayList<>();

    callMethod(
      input -> {
        List<Runnable> runnableList = input.shutdownNow();

        remaining.addAll(runnableList);

        return null;
      }
    );

    return remaining;
  }

  @Override
  public boolean isShutdown() {
    final AtomicBoolean result = new AtomicBoolean(true);

    callMethod(
      input -> {
        if (!input.isShutdown()) {
          result.set(false);
        }

        return null;
      }
    );

    return result.get();
  }

  @Override
  public boolean isTerminated() {
    final AtomicBoolean result = new AtomicBoolean(true);

    callMethod(
      input -> {
        if (!input.isTerminated()) {
          result.set(false);
        }

        return null;
      }
    );

    return result.get();
  }

  @Override
  public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
    final AtomicBoolean result = new AtomicBoolean(true);

    callMethod(
      input -> {
        if (!input.isTerminated()) {
          result.set(false);
        }

        return null;
      }
    );

    return result.get();

  }

  @Override
  public void execute(Runnable command) {
    ExecutorService executorService;

    // ick casting hack
    if (command instanceof TaggedRunnable) {
      TaggedRunnable taggedRunnable = (TaggedRunnable) command;
      executorService = executorServiceCache.get(taggedRunnable.getTag());
    } else {
      executorService = defaultExecutor;
    }

    Preconditions.checkNotNull(executorService);
    executorService.execute(command);
  }

  // will call method with every executor in the cache as well as the default
  private void callMethod(ExecutorServiceMethod method) {
    for (Entry<String, CallableSnapshot<ExecutorService, RuntimeException>> entry : executorServiceCache) {
      CallableSnapshot<ExecutorService, RuntimeException> snapshot = entry.getValue();
      ExecutorService executorService = snapshot.get();

      Preconditions.checkNotNull(executorService);
      method.execute(executorService);
    }

    method.execute(defaultExecutor);
  }

  private interface ExecutorServiceMethod extends Function<ExecutorService, Void, RuntimeException> {
    @Override
    Void execute(ExecutorService input) throws RuntimeException;
  }
}
