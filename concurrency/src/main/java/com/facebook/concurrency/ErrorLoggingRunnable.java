package com.facebook.concurrency;

import org.apache.log4j.Logger;

public class ErrorLoggingRunnable implements Runnable {
  private static final Logger LOG = Logger.getLogger(ErrorLoggingRunnable.class);
  private final Runnable delegate;

  public ErrorLoggingRunnable(Runnable delegate) {
    this.delegate = delegate;
  }

  @Override
  public void run() {
    try {
      delegate.run();
    } catch (Throwable t) {
      LOG.error("error", t);
    }
  }
}
