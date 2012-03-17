package com.facebook.concurrency;

import com.facebook.concurrency.ExecutorServiceFactory;

import java.util.concurrent.ExecutorService;

public class SingletonExecutorServiceFactory<T extends ExecutorService> 
  implements ExecutorServiceFactory<T> {
  private final T instance;

  public SingletonExecutorServiceFactory(T instance) {
    this.instance = instance;
  }

  @Override
  public T create() {
    return instance;
  }
}
