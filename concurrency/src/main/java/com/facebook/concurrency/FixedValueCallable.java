package com.facebook.concurrency;

import java.util.concurrent.Callable;

public class FixedValueCallable<V> implements Callable<V> {
  private final V value;

  public FixedValueCallable(V value) {
    this.value = value;
  }

  @Override
  public V call() throws Exception {
    return value;
  }
}
