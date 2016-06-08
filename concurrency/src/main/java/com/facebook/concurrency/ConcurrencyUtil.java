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

import com.google.common.collect.Iterators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.facebook.util.ExtRunnable;
import com.facebook.util.exceptions.ExceptionHandler;

/**
 * This is a helper class for running tasks in parallel. It uses a static, shared thread pool.
 * ParallelRunner is preferred for at least two reasons:
 *
 * 1. In a system that attempts to bound resources with executors, a ParallelExecutor may wrap
 * a bounded Executor and still respect those bounds. This has its own unbounded thread pool
 * and will never say no to any number of threads (ex: a call to parallelRun() asking for 100
 * threads gets it here no matter what, but ParallelRunner may be on an Executor
 * that bounds threads to 50, and 50 will be the actual limit
 *
 * 2. testing is much easier as you can put in MockExecutor
 *
 * This is useful in prototyping parallelism to see if it help.  See ParallelRunner's method
 * javadocs
 *
 */
public class ConcurrencyUtil {
  private static final Logger LOG = LoggerFactory.getLogger(ConcurrencyUtil.class);
  private static final AtomicLong INSTANCE_NUMBER = new AtomicLong(0);
  private static final ReadWriteLock SHUTDOWN_LOCK =
    new ReentrantReadWriteLock();
  private static final ExecutorService CACHED_EXECUTOR =
    Executors.newCachedThreadPool();
  private static final ParallelRunner PARALLEL_RUNNER = new ParallelRunner(
    CACHED_EXECUTOR, "ParallelRunExt-"
  );
  private static final int AWAIT_TERMINATION_SECONDS = 30;

  static {
    Runtime.getRuntime().addShutdownHook(
      new Thread(
        new Runnable() {
          @Override
          public void run() {
            SHUTDOWN_LOCK.writeLock().lock();

            try {
              CACHED_EXECUTOR.shutdown();
            } finally {
              SHUTDOWN_LOCK.writeLock().unlock();
            }
          }
        }
      )
    );
  }


  public static Runnable shutdownExecutorTask(final ExecutorService executor) {
    return new Runnable() {
      @Override
      public void run() {
        try {
          executor.shutdown();
          if (!executor.awaitTermination(AWAIT_TERMINATION_SECONDS, TimeUnit.SECONDS)) {
            LOG.warn(
              "executor didn't finish shutting down in {} seconds, moving on",
              AWAIT_TERMINATION_SECONDS
            );
          }
        } catch (InterruptedException e) {
          LOG.warn("interrupted shutting down executor");
        }
      }
    };
  }

  public static <E extends Exception> void parallelRunExt(
    Iterable<? extends ExtRunnable<E>> tasks,
    int numThreads,
    final ExceptionHandler<E> exceptionHandler
  ) throws E {
    parallelRunExt(tasks.iterator(), numThreads, exceptionHandler);
  }
  
  public static <E extends Exception> void parallelRunExt(
    Iterator<? extends ExtRunnable<E>> tasksIter,
    int numThreads,
    final ExceptionHandler<E> exceptionHandler
  ) throws E {
    parallelRunExt(
      tasksIter,
      numThreads,
      exceptionHandler,
      "ParallelRunExt-" + INSTANCE_NUMBER.getAndIncrement()
    );
  }

  public static <E extends Exception> void parallelRunExt(
    Iterable<? extends ExtRunnable<E>> tasks,
    int numThreads,
    final ExceptionHandler<E> exceptionHandler,
    String baseName
  ) throws E {
    parallelRunExt(tasks.iterator(), numThreads, exceptionHandler, baseName);
  }
  
  public static <E extends Exception> void parallelRunExt(
    Iterator<? extends ExtRunnable<E>> tasksIter,
    int numThreads,
    final ExceptionHandler<E> exceptionHandler,
    String baseName
  ) throws E {
    final AtomicReference<E> exception = new AtomicReference<E>();
    Iterator<Runnable> wrappedIterator =
      Iterators.transform(tasksIter, new ShortCircuitRunnable<>(exception, exceptionHandler));

    parallelRun(wrappedIterator, numThreads, baseName);

    if (exception.get() != null) {
      throw exception.get();
    }
  }

  public static void parallelRun(Iterable<? extends Runnable> tasks, int numThreads) {
    parallelRun(tasks.iterator(), numThreads);
  }
  
  public static void parallelRun(Iterator<? extends Runnable> tasksIter, int numThreads) {
    parallelRun(
      tasksIter, numThreads, "ParallelRun-" + INSTANCE_NUMBER.getAndIncrement()
    );
  }

  public static void parallelRun(
    Iterable<? extends Runnable> tasks, int numThreads, String baseName
  ) {
    parallelRun(tasks.iterator(), numThreads, baseName);
  }
  
  public static void parallelRun(
    Iterator<? extends Runnable> tasksIter, int numThreads, String baseName
  ) {
    ParallelRunner parallelRunner;

    // make sure the cached executor cannot be shutdown while we use it
    SHUTDOWN_LOCK.readLock().lock();

    try {
      if (!CACHED_EXECUTOR.isShutdown()) {
        // we can use the runner that wraps CACHED_EXECUTOR
        PARALLEL_RUNNER.parallelRun(tasksIter, numThreads, baseName);
      } else {
        // we have to create a one-off for this run
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        
        parallelRunner = new ParallelRunner(
          executor,
          "ParallelRunExt-"
        );
        parallelRunner.parallelRun(tasksIter, numThreads, baseName);
        executor.shutdown();
      }
    } finally {
      SHUTDOWN_LOCK.readLock().unlock();
    }
  }

  public static void shutdown() {
    SHUTDOWN_LOCK.writeLock().lock();
    
    try {
      CACHED_EXECUTOR.shutdown();
    } finally {
      SHUTDOWN_LOCK.writeLock().unlock();
    }
  }
}
