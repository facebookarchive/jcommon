/*
 * Copyright (C) 2018 Facebook, Inc.
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
package com.facebook.stats.concurrent;

import java.time.Clock;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * Computes sum and per-second rate with the ability to compute "till-now" rates over the last hour.
 * Uses a circular array where each element contains the all-time count at the end of a 1-second
 * window. The total sum over a given time range is computed by taking the difference between two
 * elements. Since old elements are removed on 1-second boundaries, there can be a slight over
 * estimate of the rate: in the average case of a fairly consistent rate, this is less than 1% for
 * the 1 minute rate.
 */
public class RateStat implements Stat {
  private static final int WINDOW_COUNT = 3601;

  private final String key;
  private final Clock clock;
  private final long created;
  private final AtomicLong lastRoll;
  private final long[] rollingWindows = new long[WINDOW_COUNT];
  private final LongAdder total = new LongAdder();

  private int currentOffset;

  public RateStat(String key) {
    this(key, Clock.systemUTC());
  }

  public RateStat(String key, Clock clock) {
    this.key = key;
    this.clock = clock;
    created = clock.millis() / 1000;
    lastRoll = new AtomicLong(created);
  }

  @Override
  public void update(long value) {
    rollWindows();
    total.add(value);
  }

  @Override
  public String toString() {
    return "RateStat{" + key + '}';
  }

  public Snapshot getRate() {
    return getSum().rate(clock.millis() / 1000 - created);
  }

  public Snapshot getSum() {
    rollWindows();

    synchronized (rollingWindows) {
      long total = this.total.sum();
      int offset = currentOffset + WINDOW_COUNT;

      return new Snapshot(
        "sum",
        total,
        total - rollingWindows[(offset - 3600) % WINDOW_COUNT],
        total - rollingWindows[(offset - 600) % WINDOW_COUNT],
        total - rollingWindows[(offset - 60) % WINDOW_COUNT]
      );
    }
  }

  private void rollWindows() {
    long now = clock.millis() / 1000;
    long lastRoll = this.lastRoll.get();
    //noinspection NumericCastThatLosesPrecision
    int elapsed = (int) (now - lastRoll);

    if (elapsed >= 1 && this.lastRoll.compareAndSet(lastRoll, now)) {
      long total = this.total.sum();

      if (elapsed > WINDOW_COUNT) {
        elapsed = WINDOW_COUNT;
      }

      synchronized (rollingWindows) {
        do {
          --elapsed;
          rollingWindows[currentOffset] = total;

          if (currentOffset >= WINDOW_COUNT - 1) {
            currentOffset = 0;
          } else {
            ++currentOffset;
          }
        } while (elapsed > 0);
      }
    }
  }
}
