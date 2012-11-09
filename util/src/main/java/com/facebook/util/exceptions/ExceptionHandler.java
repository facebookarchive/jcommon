package com.facebook.util.exceptions;

public interface ExceptionHandler<T extends Exception> {
  public <S extends Exception> T handle(S e);
}
