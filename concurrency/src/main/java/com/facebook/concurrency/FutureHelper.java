package com.facebook.concurrency;

import com.facebook.util.exceptions.ExceptionHandler;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * helper class that decorates FutureTask.get():
 * 1. handles FutureTask exceptions according to a handler 
 * 2. runs the underlying task at most once
 * 3. returns the result
 */

public class FutureHelper<T, E extends Exception> extends FutureTask<T> {
  private final AtomicBoolean hasRun = new AtomicBoolean(false);
  private final ExceptionHandler<E> exceptionHandler;
  private volatile boolean generated = false;
  private volatile boolean error = false;

  public FutureHelper(
    Callable<T> callable,
    ExceptionHandler<E> exceptionHandler
  ) {
    super(callable);
    this.exceptionHandler = exceptionHandler;
  }

  /**
   * runs the task once.  Other threads will block if another is running
   * the task.
   *
   * @return result of computation
   * @throws generic E
   */
  public T safeGet() throws E {
    if (hasRun.compareAndSet(false, true)) {
      run();
    }

    try {
      // Future.get() will block until there is a result ready, or until
      // at least one instance of run() above has completed
      T t = get();
      generated = true;
      return t;
    } catch (Exception e) {
      error = true;
      throw exceptionHandler.handle(e);
    }
  }

  public boolean isGenerated() {
    return generated;
  }

  public boolean isError() {
    return error;
  }
}
