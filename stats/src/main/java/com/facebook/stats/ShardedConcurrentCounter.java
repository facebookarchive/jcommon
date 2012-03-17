package com.facebook.stats;

import org.joda.time.DateTimeUtils;

import java.util.concurrent.atomic.AtomicLong;

public class ShardedConcurrentCounter {
  // this seemed to help maybe 10-15% by 
  private static int MEMORY_WORD_MULTIPLIER = 1;
  
  private volatile long value = 0;
  private final long maxStaleMillis;
  private volatile long globalLastDrainMillis = 
    DateTimeUtils.currentTimeMillis();
  private final CounterShard[] counterShards;

  public ShardedConcurrentCounter(int numShards, long maxStaleMillis) {
    this.maxStaleMillis = maxStaleMillis;
    counterShards = new CounterShard[MEMORY_WORD_MULTIPLIER*numShards];
    
    long now = DateTimeUtils.currentTimeMillis();
    long staggerMillis = maxStaleMillis / numShards;
    
    for (int i = 0; i < MEMORY_WORD_MULTIPLIER*numShards; i++) {
      long firstDrainMillis = now + (i * staggerMillis);
      
      // TODO: figure out if 1.5 make sense here?
      counterShards[i] = 
        new CounterShard(firstDrainMillis, (long)(1.5 * maxStaleMillis));
    }
  }

  public ShardedConcurrentCounter() {
    this(16, 500);
  }

  public void add(long delta) {
    counterShards[getShard()].add(delta);
  }

  private int getShard() {
    return MEMORY_WORD_MULTIPLIER*(int)Thread.currentThread().getId() % 
      counterShards.length;
  }


  public long get() {
    drainThreadToShared();
    
    return value;
  }
  
  public long getStale() {
    return value;
  }
  
  // TODO: possibly expose this publicly?
  private void updateIfStale() {
    if (DateTimeUtils.currentTimeMillis() - globalLastDrainMillis >= maxStaleMillis) {
      synchronized (counterShards) {
        if (DateTimeUtils.currentTimeMillis() - globalLastDrainMillis >= 
          maxStaleMillis
          ) {
          drainThreadToShared();
          globalLastDrainMillis = DateTimeUtils.currentTimeMillis();
        }
      }
    }
  }

  private void drainThreadToShared() {
    synchronized (counterShards) {
      for (CounterShard counterShard : counterShards) {
        value += counterShard.drain();
      }
    }
  }
  
  private class CounterShard {
    private final long frequencyMillis;
    private final AtomicLong counter = new AtomicLong(0);
    private volatile long lastDrainMillis;

    private CounterShard(long firstDrainMillis, long frequencyMillis) {
      lastDrainMillis = firstDrainMillis;
      this.frequencyMillis = frequencyMillis;
    }

    private void add(long delta) {
      if (DateTimeUtils.currentTimeMillis() - lastDrainMillis >= frequencyMillis) {
        drainThreadToShared();
        lastDrainMillis = DateTimeUtils.currentTimeMillis();
      }
      
      counter.addAndGet(delta);
    }

    private long drain() {
      return counter.getAndSet(0);
    }
  }
}
