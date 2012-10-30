package com.facebook.concurrency;

import com.facebook.util.exceptions.ExceptionHandler;

/**
 * Performs a blind cast from an exception of type S to an exception of type T.
 * NOTE: this should only be used if S will always be of type T.
 * @param <T>
 */
public class CastingExceptionHandler<T extends Exception>
  implements ExceptionHandler<T> {
  @Override
  public <S extends Exception> T handle(S e) {
    return (T) e;
  }
}
