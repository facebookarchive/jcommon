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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * combines the functionality of an ExecutorServiceFront and
 * UnstoppableExecutorService.  The net effect is a virtual executor over 
 * another. You bound the number of 'virtual threads' (Drainers) and
 * optionally a max time slice each Drainer may use. This is approximate
 * as a Drainer does not interrupt tasks running--it only checks between
 * tasks to see if the maxTimeSlice is reached and then gives up the
 * underlying thread in the delegate executor
 */
public class UnstoppableExecutorServiceFront extends UnstoppableExecutorService {
  /**
   * 
   * @param workQueue queue to use for storing tasks before executing
   * @param executor delegate executor to submit virtual threads to (Drainers)
   * @param poolName used to name the thread while being used  
   * @param maxDrainers number of virtual threads
   * @param maxTimeSlice after every task, a drainer will check and see if 
   * the maxTimeSlice has passed and will terminate, but re-submit itself
   * into the executor ('fair-share' the underlying executor)
   * @param maxTimeSliceUnit units for above
   */
  public UnstoppableExecutorServiceFront(
    BlockingQueue<Runnable> workQueue, 
    ExecutorService executor,
    String poolName,
    int maxDrainers,
    long maxTimeSlice,
    TimeUnit maxTimeSliceUnit
  ) {
    super(new ExecutorServiceFront(
      workQueue, 
      executor,
      poolName,
      maxDrainers,
      maxTimeSlice,
      maxTimeSliceUnit
    ));
  }

  public UnstoppableExecutorServiceFront(
    BlockingQueue<Runnable> workQueue, 
    ExecutorService executor,
    int maxDrainers,
    long maxTimeSlice,
    TimeUnit maxTimeSliceUnit
  ) {
    super(new ExecutorServiceFront(
      workQueue, 
      executor,
      maxDrainers,
      maxTimeSlice,
      maxTimeSliceUnit
    ));
  }

  public UnstoppableExecutorServiceFront(
    BlockingQueue<Runnable> workQueue, 
    ExecutorService executor, 
    int maxDrainers
  ) {
    this(workQueue, executor, "Drainer", maxDrainers, Long.MAX_VALUE, TimeUnit.MILLISECONDS);
  }

  public UnstoppableExecutorServiceFront(
    ExecutorService executor,
    long maxTimeSlice,
    TimeUnit maxTimeSliceUnit
  ) {
    this(
      new LinkedBlockingQueue<Runnable>(), executor, "Drainer", 1, maxTimeSlice, maxTimeSliceUnit
    );
  }
  
  public UnstoppableExecutorServiceFront(ExecutorService executor) {
    this(new LinkedBlockingQueue<Runnable>(), executor, 1);
  }
}
