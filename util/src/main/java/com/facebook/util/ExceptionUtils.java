package com.facebook.util;

import java.lang.reflect.Constructor;

public class ExceptionUtils {
  private ExceptionUtils() {
    throw new AssertionError("Not instantiable: " + ExceptionUtils.class);
  }

  public static <T extends Exception, S extends Exception> T wrap(S e, Class<T> clazz) {
    if (clazz.isAssignableFrom(e.getClass())) {
      return (T) e;
    }

    try {
      Constructor<T> constructor = clazz.getConstructor(Throwable.class);

      // get the exception constructor with one argument
      return constructor.newInstance(e);
    } catch (RuntimeException exception) {
      throw exception;
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }
}
