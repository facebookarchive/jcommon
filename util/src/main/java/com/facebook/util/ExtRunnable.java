package com.facebook.util;

/**
 * extended runnable that throws an exception. Does not a produce a value
 * (hence not a Callable)
 * 
 * @param <E> type of exception you want to be able to throw
 */
public interface ExtRunnable<E extends Throwable>{
  public void run() throws E;
}
