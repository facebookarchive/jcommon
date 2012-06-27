package com.facebook.concurrency;

/**
 * implementations may vary wildly, but typically this is for an object that
 * wants to do any cleanup or bookkeeping when it's finished
 */
public interface Completable {
  public void complete();
}
