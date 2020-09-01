/*
 * Copyright (C) 2012 Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.stats;

import java.util.concurrent.atomic.AtomicLong;
import org.joda.time.DateTimeUtils;

public class ShardedConcurrentCounter {
  // this seemed to help maybe 10-15% by
  private static int MEMORY_WORD_MULTIPLIER = 1;

  private volatile long value = 0;
  private final long maxStaleMillis;
  private volatile long globalLastDrainMillis = DateTimeUtils.currentTimeMillis();
  private final CounterShard[] counterShards;

  public ShardedConcurrentCounter(int numShards, long maxStaleMillis) {
    this.maxStaleMillis = maxStaleMillis;
    counterShards = new CounterShard[MEMORY_WORD_MULTIPLIER * numShards];

    long now = DateTimeUtils.currentTimeMillis();
    long staggerMillis = maxStaleMillis / numShards;

    for (int i = 0; i < MEMORY_WORD_MULTIPLIER * numShards; i++) {
      long firstDrainMillis = now + (i * staggerMillis);

      // TODO: figure out if 1.5 make sense here?
      counterShards[i] = new CounterShard(firstDrainMillis, (long) (1.5 * maxStaleMillis));
    }
  }

  public ShardedConcurrentCounter() {
    this(16, 500);
  }

  public void add(long delta) {
    counterShards[getShard()].add(delta);
  }

  private int getShard() {
    return MEMORY_WORD_MULTIPLIER * (int) Thread.currentThread().getId() % counterShards.length;
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
        if (DateTimeUtils.currentTimeMillis() - globalLastDrainMillis >= maxStaleMillis) {
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
