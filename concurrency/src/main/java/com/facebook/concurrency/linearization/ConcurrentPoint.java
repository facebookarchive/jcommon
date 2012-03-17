package com.facebook.concurrency.linearization;

/**
 * Effectively a read lock. Once start() is called, any call to
 * LinearizationPoint.start() will block 
 *
 * NOTE: use start/complete in a try/finally block the same as Lock
 */
public interface ConcurrentPoint {
  public void start();
  public void complete();
}
