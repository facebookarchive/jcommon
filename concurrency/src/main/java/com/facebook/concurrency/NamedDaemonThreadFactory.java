package com.facebook.concurrency;

/**
 * A thread factory that marks all threads that it creates as daemon threads.
 */
public class NamedDaemonThreadFactory extends NamedThreadFactory {
  public NamedDaemonThreadFactory(String baseName) {
    super(baseName);
  }

  @Override
  public Thread newThread(Runnable r) {
    final Thread thread = super.newThread(r);
    thread.setDaemon(true);
    return thread;
  }
}
