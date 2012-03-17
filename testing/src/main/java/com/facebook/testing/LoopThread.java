package com.facebook.testing;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * used only with ThreadHelper
 */
public class LoopThread {
  private final Thread thread;
  private final AtomicBoolean condition;

  /**
   * @param thread
   * @param condition serves as simple Observer.  Will be set to false
   * when someone tries to calls join()
   * 
   * often used by the underlying Runnable to know that someone may
   * be ready for this to terminate
   */
  LoopThread(Thread thread, AtomicBoolean condition) {
    this.thread = thread;
    this.condition = condition;
  }

  public void start() {
    thread.start();
  }
  
  public String getName() {
    return thread.getName();
  }

  public void join() throws InterruptedException {
    condition.set(false);
    thread.join();
  }
}
