package com.facebook.concurrency;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * combines the funtionality of an ExecutorServiceFront and 
 * UnstoppableExecutorService.  The net effect is a virtual executor over 
 * annother. You bound the number of 'virtual threads' (Drainers) and 
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
   * into the executor ('fair-share' the underlying executor
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
    this(
      workQueue, executor, "Drainer", maxDrainers, Long.MAX_VALUE, TimeUnit.MILLISECONDS);
  }

  public UnstoppableExecutorServiceFront(
    ExecutorService executor,
    long maxTimeSlice,
    TimeUnit maxTimeSliceUnit
  ) {
    this(
      new LinkedBlockingQueue<Runnable>(), 
      executor, 
      "Drainer",
      1, 
      maxTimeSlice,
      maxTimeSliceUnit);
  }
  
  public UnstoppableExecutorServiceFront(ExecutorService executor) {
    this(new LinkedBlockingQueue<Runnable>(), executor, 1);
  }
}
