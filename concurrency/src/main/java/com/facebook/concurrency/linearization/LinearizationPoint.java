package com.facebook.concurrency.linearization;

import java.util.concurrent.TimeUnit;

/**
 * similar to a write lock, will block other ConcurrentPoints and
 * LinearizationPoints.
 * 
 * * NOTE: use start/complete in a try/finally block the same as Lock
 */
public interface LinearizationPoint {
  public void start();
  public void complete();
  public void waitForStart() throws InterruptedException;
  public boolean waitForStart(long timeout, TimeUnit unit) 
    throws InterruptedException;
  public void waitForCompletion() throws InterruptedException;
  public boolean waitForCompletion(long timeout, TimeUnit unit) 
    throws InterruptedException;
}
