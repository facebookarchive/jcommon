package com.facebook.concurrency;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

class BlockingValueProducer<V, E extends Exception> implements Callable<V> {
  private final V value;
  private final E ex;
  private final CountDownLatch latch;
  private AtomicInteger calledCount = new AtomicInteger(0);
  private AtomicInteger completedCount = new AtomicInteger(0);

  BlockingValueProducer(V value, boolean blocked, E ex) {
    this.value = value;
    this.ex = ex;

    if (blocked) {
      latch = new CountDownLatch(1);
    } else {
      latch = new CountDownLatch(0);
    }
  }

  BlockingValueProducer(V value, boolean blocked) {
    this(value, blocked, null);
  }

  BlockingValueProducer(V value) {
    this(value, false, null);
  }

  @Override
  public V call() throws E {
    try {
      latch.await();
    } catch (InterruptedException e) {
      // Ignore
    }
    calledCount.incrementAndGet();

    if (ex != null) {
      throw ex;
    }

    completedCount.incrementAndGet();

    return value;
  }

  public int getCalledCount() {
    return calledCount.get();
  }

  public int getCompletedCount() {
    return completedCount.get();
  }

  public void signal() {
    latch.countDown();
  }
}
