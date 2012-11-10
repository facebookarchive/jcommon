package com.facebook.concurrency;

public interface ExceptionHandler<T extends Exception> {
  public <S extends Exception> T handle(S e);
}
