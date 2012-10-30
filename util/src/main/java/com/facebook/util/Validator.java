package com.facebook.util;

import com.google.common.base.Preconditions;

public class Validator<T extends Exception> {
  private final Class<T> clazz;

  public Validator(Class<T> clazz) {
    this.clazz = clazz;
  }

  public void checkState(boolean expression) throws T {
    try {
      Preconditions.checkState(expression);
    } catch (Exception e) {
      throw ExceptionUtils.wrap(e, clazz);
    }
  }

  public void checkState( boolean expression, String message) throws T {
    try {
      Preconditions.checkState(expression, message);
    } catch (Exception e) {
      throw ExceptionUtils.wrap(e, clazz);
    }
  }

  public void checkState( boolean expression, String message, Object... errorMessageArgs)
    throws T {
    try {
      Preconditions.checkState(expression, message, errorMessageArgs);
    } catch (Exception e) {
      throw ExceptionUtils.wrap(e, clazz);
    }
  }
}
