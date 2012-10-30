package com.facebook.concurrency;

import com.facebook.util.exceptions.ExceptionHandler;

import java.util.concurrent.Callable;

public class CallableSnapshot<V, E extends Exception> {
  private V value = null;
  private E exception = null;

  public CallableSnapshot(
    Callable<V> callable, ExceptionHandler<E> exceptionHandler
  ) {
    try {
      value = callable.call();
    } catch (Exception e) {
      exception = exceptionHandler.handle(e);
    }
  }

  public V get() throws E {
    if (exception != null) {
      throw exception;
    }
    return value;
  }

  public E getException() {
    return exception;
  }
}
