package com.facebook.util;

import com.google.common.base.Preconditions;

public class Validations {
  private Validations() {
  }

  public static <T extends Exception> void checkState(boolean expression, Class<T> clazz) throws T {
    try {
      Preconditions.checkState(expression);
    } catch (Exception e) {
      throw ExceptionUtils.wrap(e, clazz);
    }
  }

  public static <T extends Exception> void checkState(
    boolean expression, Class<T> clazz, String message
  ) throws T {
    try {
      Preconditions.checkState(expression, message);
    } catch (Exception e) {
      throw ExceptionUtils.wrap(e, clazz);
    }
  }

  public static <T extends Exception> void checkState(
    boolean expression, Class<T> clazz, String message, Object... errorMessageArgs
  ) throws T {
    try {
      Preconditions.checkState(expression, message, errorMessageArgs);
    } catch (Exception e) {
      throw ExceptionUtils.wrap(e, clazz);
    }
  }
}
