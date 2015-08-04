package com.facebook.concurrency;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class StratifiedExecutorServiceBuilder {

  private final ExecutorServiceFactory<ExecutorService> coreExecutorServiceFactory;

  private int maxGlobalThreads = 1024;
  private int maxThreadsPerTag = 1;

  StratifiedExecutorServiceBuilder(
    ExecutorServiceFactory<ExecutorService> coreExecutorServiceFactory
  ) {
    this.coreExecutorServiceFactory = coreExecutorServiceFactory;
  }

  public StratifiedExecutorServiceBuilder(final ThreadFactory threadFactory) {
    this(
      () -> {
        return Executors.newCachedThreadPool(threadFactory);
      }
    );
  }

  public StratifiedExecutorServiceBuilder setMaxGlobalThreads(int maxGlobalThreads) {
    this.maxGlobalThreads = maxGlobalThreads;

    return this;
  }

  public StratifiedExecutorServiceBuilder setMaxThreadsPerTag(int maxThreadsPerTag) {
    this.maxThreadsPerTag = maxThreadsPerTag;

    return this;
  }

  public StratifiedExecutorService build() {
    // builder that returns an ExecutorServiceFront (ExecutorService that uses 'virtual threads'
    ExecutorService coreExecutor = coreExecutorServiceFactory.create();
    final ExecutorServiceFrontBuilder executorServiceFrontBuilder = new ExecutorServiceFrontBuilder(
      coreExecutor, maxGlobalThreads
    ).setMaxInstanceThreads(maxThreadsPerTag);
    ExecutorServiceFactory<ExecutorService> executorServiceFactory = () -> {
      ExecutorServiceFront front = executorServiceFrontBuilder.build();
      UnstoppableExecutorServiceFront unstoppableExecutorServiceFront = new UnstoppableExecutorServiceFront(front);

      return unstoppableExecutorServiceFront;
    };
    StratifiedExecutorService executorService = new StratifiedExecutorService(executorServiceFactory, coreExecutor);

    return executorService;
  }
}
