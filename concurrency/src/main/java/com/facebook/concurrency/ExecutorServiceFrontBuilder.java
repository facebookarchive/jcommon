package com.facebook.concurrency;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ExecutorServiceFrontBuilder {
  private final ExecutorService coreExecutor;
  private int maxInstanceThreads = 1;
  private long maxTimeSlice = Long.MAX_VALUE;
  private TimeUnit maxTimeSliceUnit = TimeUnit.SECONDS;
  private String drainerBaseName = "Drainer";

  /**
   * Basic constructor
   *
   * @param coreExecutor
   */
  public ExecutorServiceFrontBuilder(ExecutorService coreExecutor) {
    this.coreExecutor = coreExecutor;
  }

  /**
   * Constructor that limits the maximum number of threads that can be used
   * from the core executor
   *
   * @param coreExecutor
   * @param maxCoreThreads
   */
  public ExecutorServiceFrontBuilder(
    ExecutorService coreExecutor, int maxCoreThreads
  ) {
    this.coreExecutor = new ExecutorServiceFront(
      new LinkedBlockingQueue<Runnable>(),
      coreExecutor,
      maxCoreThreads
    );
  }

  /**
   * Limit the maximum number of threads that can be used by a single
   * constructed ExecutorServiceFront
   *
   * @param maxInstanceThreads
   * @return
   */
  public ExecutorServiceFrontBuilder setMaxInstanceThreads(
    int maxInstanceThreads
  ) {
    this.maxInstanceThreads = maxInstanceThreads;
    return this;
  }

  /**
   * set the base name
   * 
   * @param name name to use
   */
  public ExecutorServiceFrontBuilder setDrainerBaseName(String name) {
    this.drainerBaseName = name;
    
    return this;
  }

  /**
   * Limit the maximum time slice that a constructed ExecutorServiceFront
   * drainer can be run
   *
   * @param maxTimeSlice
   * @param maxTimeSliceUnit
   * @return
   */
  public ExecutorServiceFrontBuilder setMaxTimeSlice(
    long maxTimeSlice, TimeUnit maxTimeSliceUnit
  ) {
    this.maxTimeSlice = maxTimeSlice;
    this.maxTimeSliceUnit = maxTimeSliceUnit;
    return this;
  }

  public ExecutorServiceFront build() {
    return new ExecutorServiceFront(
      new LinkedBlockingQueue<Runnable>(),
      coreExecutor,
      drainerBaseName,
      maxInstanceThreads,
      maxTimeSlice,
      maxTimeSliceUnit
    );
  }
}
