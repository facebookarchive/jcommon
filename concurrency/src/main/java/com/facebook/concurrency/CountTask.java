package com.facebook.concurrency;

import java.util.concurrent.atomic.AtomicLong;

public class CountTask implements Runnable {
  private final AtomicLong count = new AtomicLong(0);

  @Override
  public void run() {
    count.incrementAndGet();
  }
  
  public long getValue() {
    return count.get();
  }
}
