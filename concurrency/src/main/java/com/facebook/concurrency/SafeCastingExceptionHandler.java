package com.facebook.concurrency;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Performs a blind cast from an exception of type S to an exception of type T.
 * NOTE: this should only be used if S will always be of type T.
 * @param <T>
 */
public class SafeCastingExceptionHandler<T extends Exception>
  implements ExceptionHandler<T> {
  
  private final Class<T> exceptionClass;

  public SafeCastingExceptionHandler(Class<T> exceptionClass) {
    this.exceptionClass = exceptionClass;
  }

  @Override
  public <S extends Exception> T handle(S e) {

    if (exceptionClass.isAssignableFrom(e.getClass())) {
      // this cast is in fact safe since we know e is an instanceof Class<T>
      //noinspection unchecked
      return (T)e;
    } else {
      try {
        Constructor<T> constructor = exceptionClass.getConstructor(
          Throwable.class
        );
        // get the exception constructor with one argument
        return constructor.newInstance(e);
      } catch (InstantiationException e1) {
          throw new RuntimeException(e1);
      } catch (IllegalAccessException e1) {
        throw new RuntimeException(e1);
      } catch (InvocationTargetException e1) {
        throw new RuntimeException(e1);
      } catch (NoSuchMethodException e1) {
        throw new RuntimeException(e1);
      }
    }
  }
}
