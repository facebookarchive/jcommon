package com.facebook.concurrency;

/**
 * translates any exception to 'null'. Use only when you know the result
 * won't be thrown 
 * 
 * @param <T>
 */
public class NullExceptionHandler<T extends Exception> implements
  ExceptionHandler<T> {
  @Override
  public <S extends Exception> T handle(S e) {
    return null;
  }
}
