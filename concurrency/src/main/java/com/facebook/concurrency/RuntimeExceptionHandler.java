package com.facebook.concurrency;

import com.facebook.util.exceptions.ExceptionHandler;

/**
 * map any exception to a runtime exception
 */
public class RuntimeExceptionHandler implements ExceptionHandler<RuntimeException> {
  public static RuntimeExceptionHandler INSTANCE = new RuntimeExceptionHandler();

  @Override
  public <S extends Exception> RuntimeException handle(S e) {
    return new RuntimeException(e.getMessage(), e);
  }
}
