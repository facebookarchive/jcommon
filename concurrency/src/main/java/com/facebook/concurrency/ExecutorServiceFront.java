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
import org.joda.time.DateTimeUtils;

import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class puts its own Queue in front of another ExecutorService.
 * It uses that service to submit tasks that drain its queue.  It ensures
 * a max # of drainer threads (default 1)
 * 
 * Typical use case : back this by a SynchronousQueue based thread pool, ie
 * Executors.newCachedThreadPool() 
 * 
 * This will allow more than one object to share the thread pool, but have
 * a maximum # of threads.  When there are no elements in the queue,
 * no threads are used.
 * 
 */
public class ExecutorServiceFront extends AbstractExecutorService {
  private final Lock lock = new ReentrantLock();
  private final BlockingQueue<Runnable> workQueue;
  private final ExecutorService executor;
  private final long maxTimeSliceMillis;
  private final BlockingQueue<Drainer> drainerList;

  private static final Logger LOG = LoggerFactory.getLogger(ExecutorServiceFront.class);

  /**
   * 
   * @param workQueue
   * @param executor
   * @param maxDrainers
   * @param maxTimeSlice - the maximum time slice of a drainer can run
   * @param maxTimeSliceUnit - the unit of the maxTimeSlice argument
   */
  public ExecutorServiceFront(
    BlockingQueue<Runnable> workQueue, 
    ExecutorService executor,
    String poolName,
    int maxDrainers,
    long maxTimeSlice,
    TimeUnit maxTimeSliceUnit
  ) {
    this.workQueue = workQueue;
    this.executor = executor;
    this.maxTimeSliceMillis = maxTimeSliceUnit.toMillis(maxTimeSlice);
    drainerList = new ArrayBlockingQueue<>(maxDrainers);
    
    for (int i = 0; i < maxDrainers; i++) {
      drainerList.add(new Drainer(String.format("%s-%03d", poolName, i)));
    }
  }

  public ExecutorServiceFront(
    BlockingQueue<Runnable> workQueue, 
    ExecutorService executor,
    int maxDrainers,
    long maxTimeSlice,
    TimeUnit maxTimeSliceUnit
  ) {
    this(workQueue, executor, "Drainer", maxDrainers, maxTimeSlice, maxTimeSliceUnit);
  }

  public ExecutorServiceFront(
    BlockingQueue<Runnable> workQueue, 
    ExecutorService executor,
    String poolName,
    int maxDrainers
  ) {
    this(workQueue, executor, poolName, maxDrainers, Long.MAX_VALUE, TimeUnit.MILLISECONDS);
  }

  public ExecutorServiceFront(
    BlockingQueue<Runnable> workQueue, 
    ExecutorService executor, 
    int maxDrainers
  ) {
    this(workQueue, executor, "Drainer", maxDrainers, Long.MAX_VALUE, TimeUnit.MILLISECONDS);
  }

  public ExecutorServiceFront(
    ExecutorService executor,
    long maxTimeSlice,
    TimeUnit maxTimeSliceUnit
  ) {
    this(
      new LinkedBlockingQueue<>(),
      executor, 
      "Drainer",
      1, 
      maxTimeSlice,
      maxTimeSliceUnit);
  }
  
  public ExecutorServiceFront(ExecutorService executor) {
    this(new LinkedBlockingQueue<>(), executor, 1);
  }

  @Override
  public void shutdown() {
    throw new UnsupportedOperationException();
  }

  @Override
  public synchronized List<Runnable> shutdownNow() {
    throw new UnsupportedOperationException();    
  }

  @Override
  public boolean isShutdown() {
    throw new UnsupportedOperationException();    
  }

  @Override
  public boolean isTerminated() {
    throw new UnsupportedOperationException();    
  }

  @Override
  public boolean awaitTermination(long timeout, TimeUnit unit)
    throws InterruptedException {
    throw new UnsupportedOperationException();    
  }

  @Override
  public void execute(Runnable task) {
    workQueue.offer(task);
    lock.lock();

    try {
      if (!drainerList.isEmpty()) {
        executor.execute(drainerList.poll());
      }
    } finally {
      lock.unlock();
    }
  }

  private class Drainer implements Runnable {
    private final String threadName;

    private Drainer(String threadName) {
      this.threadName = threadName;
    }

    public void run() {
      Thread t = Thread.currentThread();
      String oldName = t.getName();
      t.setName(threadName);

      try {
        internalRun();
      } finally {
        t.setName(oldName);
      }
    }

    private void internalRun() {
      long startTime = DateTimeUtils.currentTimeMillis();

      while (DateTimeUtils.currentTimeMillis() - startTime < maxTimeSliceMillis) {
        Runnable task = null;

        lock.lock();
        
        try {
          task = workQueue.poll();

          if (task == null) {
            drainerList.add(this);

            return;
          }
        } finally {
          lock.unlock();
        }

        try {
          task.run();
        } catch (RuntimeException e) {
          LOG.warn("Ignoring Task Failure", e);
        }
      }

      lock.lock();

      try {
        // NOTE: if our queue is empty here, subsequent execute() will create new Drainers
        // if need be. There is an edge case that executor is shutdown and we have no tasks,
        // which is why we don't re-submit ourselves unless we have work to do (ie we haven't
        // terminated
        if (workQueue.isEmpty()) {
          // if there's no work, this drainer expires
          drainerList.add(this);
        } else {
          executor.execute(this);
        }
      } finally {
        lock.unlock();
      }
    }
  }
}
