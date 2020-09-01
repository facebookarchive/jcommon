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
   * Constructor that limits the maximum number of threads that can be used from the core executor
   *
   * @param coreExecutor
   * @param maxCoreThreads
   */
  public ExecutorServiceFrontBuilder(ExecutorService coreExecutor, int maxCoreThreads) {
    this.coreExecutor =
        new ExecutorServiceFront(new LinkedBlockingQueue<Runnable>(), coreExecutor, maxCoreThreads);
  }

  /**
   * Limit the maximum number of threads that can be used by a single constructed
   * ExecutorServiceFront
   *
   * @param maxInstanceThreads
   * @return
   */
  public ExecutorServiceFrontBuilder setMaxInstanceThreads(int maxInstanceThreads) {
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
   * Limit the maximum time slice that a constructed ExecutorServiceFront drainer can be run
   *
   * @param maxTimeSlice
   * @param maxTimeSliceUnit
   * @return
   */
  public ExecutorServiceFrontBuilder setMaxTimeSlice(long maxTimeSlice, TimeUnit maxTimeSliceUnit) {
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
        maxTimeSliceUnit);
  }
}
