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

import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import com.facebook.util.ExtRunnable;
import com.facebook.util.exceptions.ExceptionHandler;

/**
 * Utility class in order to execute tasks in parallel on top of an executor, but bound the
 * number of concurrent tasks used in that executor.  Note, if the executor itself has a bound
 * lower than specified, that bound will of course be used.
 */
public class ParallelRunner {
  private static final Logger LOG = LoggerFactory.getLogger(ParallelRunner.class);
  private static final String DEFAULT_NAME_PREFIX = "ParallelRun-";

  private final AtomicLong instanceNumber = new AtomicLong(0);

  private final ExecutorService executor;
  private final String defaultNamePrefix;

  /**
   * Create an instance on top of an underlying executor
   * 
   * @param executor executor to wrap
   * @param defaultNamePrefix borrowed threads will use this name prefix
   */
  public ParallelRunner(ExecutorService executor, String defaultNamePrefix) {
    this.executor = executor;
    this.defaultNamePrefix = defaultNamePrefix;
  }

  /**
   * use a default naming prefix, ParallelRunner.DEFAULT_NAME_PREFIX
   * 
   * @param executor
   */
  public ParallelRunner(ExecutorService executor) {
    this(executor, DEFAULT_NAME_PREFIX);
  }

  /**
   * Helper using default name ParallelRunner.DEFAULT_NAME_PREFIX and Iterables of ExtRunnables
   *
   * @param tasks
   * @param numThreads
   * @param exceptionHandler
   * @param <E>
   * @throws E
   */
  public <E extends Exception> void parallelRunExt(
    Iterable<? extends ExtRunnable<E>> tasks,
    int numThreads,
    final ExceptionHandler<E> exceptionHandler
  ) throws E {
    parallelRunExt(tasks.iterator(), numThreads, exceptionHandler);
  }

  /**
   * Helper using default name ParallelRunner.DEFAULT_NAME_PREFIX
   *
   * @param tasksIter
   * @param numThreads
   * @param exceptionHandler
   * @param <E>
   * @throws E
   */
  public <E extends Exception> void parallelRunExt(
    Iterator<? extends ExtRunnable<E>> tasksIter,
    int numThreads,
    final ExceptionHandler<E> exceptionHandler
  ) throws E {
    parallelRunExt(
      tasksIter,
      numThreads,
      exceptionHandler,
      defaultNamePrefix + instanceNumber.getAndIncrement()
    );
  }

  /**
   * Helper function for Iterables and ExtRunnables
   *
   * @param tasks
   * @param numThreads
   * @param exceptionHandler
   * @param baseName
   * @param <E>
   * @throws E
   */
  public <E extends Exception> void parallelRunExt(
    Iterable<? extends ExtRunnable<E>> tasks,
    int numThreads,
    final ExceptionHandler<E> exceptionHandler,
    String baseName
  ) throws E {
    parallelRunExt(tasks.iterator(), numThreads, exceptionHandler, baseName);
  }

  /**
   * Adapter methods for ExtRunnable<E> to convert to native Runnable format. An ExceptionHandler
   * will be used to guarantee type E is thrown, and only one, the "first" exception will be
   * thrown. The system is fail-fast in that once a task execution observes an exception has
   * occurred, it does not run additional tasks.
   *
   * It has the same contract as far as executing tasks as they are extracted from the Iterator
   *
   * @param tasksIter
   * @param numThreads
   * @param exceptionHandler
   * @param baseName
   * @param <E>
   * @throws E
   */
  public <E extends Exception> void parallelRunExt(
    Iterator<? extends ExtRunnable<E>> tasksIter,
    int numThreads,
    final ExceptionHandler<E> exceptionHandler,
    String baseName
  ) throws E {
    final AtomicReference<E> exception = new AtomicReference<E>();
    Iterator<Runnable> wrappedIterator = Iterators.transform(
      tasksIter, new Function<ExtRunnable<E>, Runnable>() {
      @Override
      public Runnable apply(final ExtRunnable<E> task) {
        return new Runnable() {
          @Override
          public void run() {
            try {
              // short-circuit if other tasksIter failed
              if (exception.get() == null) {
                task.run();
              }
            } catch (Exception e) {
              exception.compareAndSet(null, exceptionHandler.handle(e));
            }
          }
        };
      }
    });

    parallelRun(wrappedIterator, numThreads, baseName);

    if (exception.get() != null) {
      throw exception.get();
    }
  }

  /**
   * helper method with default name prefix ParallelRunner.DEFAULT_NAME_PREFIX for Iterabless
   *
   * @param tasks
   * @param numThreads
   */
  public void parallelRun(Iterable<? extends Runnable> tasks, int numThreads) {
    parallelRun(tasks.iterator(), numThreads);
  }

  /**
   * helper method with default name prefix ParallelRunner.DEFAULT_NAME_PREFIX
   * @param tasksIter
   * @param numThreads
   */
  public void parallelRun(Iterator<? extends Runnable> tasksIter, int numThreads) {
    parallelRun(
      tasksIter,
      numThreads,
      defaultNamePrefix + instanceNumber.getAndIncrement()
    );
  }

  /**
   * adapter method for Iterables
   * 
   * @param tasks
   * @param numThreads
   * @param baseName
   */
  public void parallelRun(
    Iterable<? extends Runnable> tasks, int numThreads, String baseName
  ) {
    parallelRun(tasks.iterator(), numThreads, baseName);
  }

  /**
   * This is the core method of ParallelRunner , which takes an iterator of tasks. It is ideal as 
   * often it is desirable to begin execution of tasks before the entire set has been created.  In
   * this way, task are started immediately as they are pulled off of the iterator than than
   * draining the iterator and then executing them.
   *
   * Clients may use this fact and create Iterators that are more of a "queue" and take advantage
   * of this fact. Another way to look at this is as this is a consumer of tasks that come from a
   * producer (iterator). The expectation is that eventually, most use cases will eventually quit
   * producing tasks, and hence taskIter.hasNext() return false.
   *
   * There is nothing in the implementation that requires this, however, and if a client
   * constructs an unbounded Iterator, this will function correctly.
   *
   * @param tasksIter
   * @param numThreads
   * @param baseName
   */
  public void parallelRun(
    Iterator<? extends Runnable> tasksIter, int numThreads, String baseName
  ) {
    ExecutorService executorForInvocation;
    
    // create a virtual executor that bounds the # of threads we can use
    // for this run
    executorForInvocation =
      new UnstoppableExecutorService(
        new ExecutorServiceFront(
          new LinkedBlockingQueue<Runnable>(),
          executor,
          baseName,
          numThreads
        )
      );
    
    int totalTasks = 0;

    while (tasksIter.hasNext()) {
      executorForInvocation.execute(tasksIter.next());
      totalTasks++;
    }
    
    // now wait for everything to finish
    executorForInvocation.shutdown();
    
    try {
      while (!executorForInvocation.awaitTermination(10, TimeUnit.SECONDS)) {
        LOG.info(
          "({}) {} waited 10s for {} tasks, waiting some more",
          Thread.currentThread().getId(),
          baseName,
          totalTasks
        );
      }

      LOG.info(
          "({}) tasksIter for {} completed",
          Thread.currentThread().getId(),
          baseName
      );
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();

      LOG.warn("interrupted waiting for tasks to complete", e);
    }
  }
}
