package com.facebook.concurrency;
  
import org.testng.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConcurrentCacheTestHelper<K, V> {
  private final ConcurrentCache<K, V, RuntimeException> cache;
  private final List<Throwable> exceptionList = 
    Collections.synchronizedList(new ArrayList<Throwable>());

  public ConcurrentCacheTestHelper(
    ConcurrentCache<K, V, RuntimeException> cache
  ) {
    this.cache = cache;
  }

  Thread clearInThread() {
    return doInThread(new Runnable() {
      @Override
      public void run() {
        cache.clear();
      }
    });
  }
  
  Thread getInThread(
    final K key,
    final V expectedValue
  ) {
    return doInThread(new Runnable() {
      @Override
      public void run() {
        // we should get the expected value on a cache-miss
        Assert.assertEquals(cache.get(key), expectedValue);
      }
    });
  }

  Thread removeInThread(final K key, final V expectedValue) {
    return doInThread(new Runnable() {
      @Override
      public void run() {
        // we should get the expected value on a cache-miss
        Assert.assertEquals(cache.remove(key), expectedValue);
      }
    });
  }

  Thread doInThread(Runnable operation) {
    Thread t = new Thread(operation);

    // need to make sure we propagate the exception
    t.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
      @Override
      public void uncaughtException(Thread t, Throwable e) {
        exceptionList.add(e);
      }
    });
    t.start();

    return t;
  }

  List<Throwable> getExceptionList() {
    return exceptionList;
  }
}
