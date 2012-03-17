package com.facebook.testing;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class AnnotatedRunnable implements Runnable {
  private final long initDelay;
  private final long delay;
  private final TimeUnit unit;
  private final Runnable delegate;
  private ScheduledFuture<?> future;

  public AnnotatedRunnable(
    Runnable delegate, long initDelay, long delay, TimeUnit unit
  ) {
    this.delegate = delegate;
    this.initDelay = initDelay;
    this.delay = delay;
    this.unit = unit;
  }

  public AnnotatedRunnable(Runnable delegate) {
    this(delegate, -1, -1, TimeUnit.SECONDS);
  }

  @Override
  public void run() {
    delegate.run();
  }

  public long getInitDelay() {
    return initDelay;
  }

  public long getDelay() {
    return delay;
  }

  public TimeUnit getUnit() {
    return unit;
  }

  public ScheduledFuture<?> getFuture() {
    return future;
  }

  public void setFuture(ScheduledFuture<?> future) {
    this.future = future;
  }
}
