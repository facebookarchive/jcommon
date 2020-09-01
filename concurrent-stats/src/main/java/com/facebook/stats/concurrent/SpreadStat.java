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

import static java.lang.Long.MAX_VALUE;
import static java.lang.Long.MIN_VALUE;

import java.time.Clock;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * Computes sum, average, sample count, min, max and per-second rate with the ability to compute
 * "till-now" values over the last hour. Uses a circular arrays where each element contains the
 * all-time (in the case of sum and count) or interval (in the case of min/max) value at the end of
 * a 1-second window. The total sum or count over a given time range is computed by taking the
 * difference between two elements. The min/max is computed by iterating over the windows in the
 * range. Since old elements are removed on 1-second boundaries, there can be a slight over estimate
 * of the rate and count: in the average case of a fairly consistent rate, this is less than 1% for
 * the 1 minute rate or count.
 */
public class SpreadStat implements Stat {
  private static final int WINDOW_COUNT = 3601;
  private static final int TOTAL_INDEX = 0;
  private static final int COUNT_INDEX = 1;
  private static final int MIN_INDEX = 2;
  private static final int MAX_INDEX = 3;

  private final String key;
  private final Clock clock;
  private final long created;
  private final AtomicLong lastRoll;
  private final long[][] rollingWindows = new long[WINDOW_COUNT][4];
  private final LongAdder total = new LongAdder();
  private final LongAdder count = new LongAdder();
  private final AtomicLong currentMin = new AtomicLong(MAX_VALUE);
  private final AtomicLong currentMax = new AtomicLong(MIN_VALUE);

  private long min = MAX_VALUE;
  private long max = MIN_VALUE;
  private int currentOffset;

  public SpreadStat(String key) {
    this(key, Clock.systemUTC());
  }

  public SpreadStat(String key, Clock clock) {
    this.key = key;
    this.clock = clock;
    created = clock.millis() / 1000;
    lastRoll = new AtomicLong(created);

    for (int i = 0; i < rollingWindows.length; ++i) {
      rollingWindows[i][MIN_INDEX] = MAX_VALUE;
      rollingWindows[i][MAX_INDEX] = MIN_VALUE;
    }
  }

  @Override
  public void update(long value) {
    rollWindows();

    long minValue = currentMin.get();

    while (value < minValue && !currentMin.compareAndSet(minValue, value)) {
      minValue = currentMin.get();
    }

    long maxValue = currentMax.get();

    while (value > maxValue && !currentMax.compareAndSet(maxValue, value)) {
      maxValue = currentMax.get();
    }

    total.add(value);
    count.increment();
  }

  @Override
  public String toString() {
    return "SpreadStat{" + key + '}';
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
          total - rollingWindows[(offset - 3600) % WINDOW_COUNT][TOTAL_INDEX],
          total - rollingWindows[(offset - 600) % WINDOW_COUNT][TOTAL_INDEX],
          total - rollingWindows[(offset - 60) % WINDOW_COUNT][TOTAL_INDEX]);
    }
  }

  public Snapshot getSamples() {
    rollWindows();

    synchronized (rollingWindows) {
      long count = this.count.sum();
      int offset = currentOffset + WINDOW_COUNT;

      return new Snapshot(
          "samples",
          count,
          count - rollingWindows[(offset - 3600) % WINDOW_COUNT][COUNT_INDEX],
          count - rollingWindows[(offset - 600) % WINDOW_COUNT][COUNT_INDEX],
          count - rollingWindows[(offset - 60) % WINDOW_COUNT][COUNT_INDEX]);
    }
  }

  public Snapshot getAverage() {
    Snapshot total;
    Snapshot count;
    int currentOffset;

    rollWindows();

    synchronized (rollingWindows) {
      do {
        // It's possible, though highly unlikely, that getSamples() will roll the current window.
        currentOffset = this.currentOffset;
        total = getSum();
        count = getSamples();
      } while (currentOffset != this.currentOffset);
    }

    return new Snapshot(
        "average",
        average(total.getAllTime(), count.getAllTime()),
        average(total.getHour(), count.getHour()),
        average(total.getTenMinute(), count.getTenMinute()),
        average(total.getMinute(), count.getMinute()));
  }

  public Snapshot getMin() {
    rollWindows();

    synchronized (rollingWindows) {
      int offset = currentOffset + WINDOW_COUNT;
      long hourMin = currentMin.get();
      long minuteMin = hourMin;
      long tenMinuteMin = hourMin;

      // Current second is in currentMin, so start at 1, not 0
      for (int i = 1; i < 3600; ++i) {
        if (i == 60) {
          minuteMin = hourMin;
        } else if (i == 600) {
          tenMinuteMin = hourMin;
        }

        long windowMin = rollingWindows[(offset - i) % WINDOW_COUNT][MIN_INDEX];

        if (hourMin > windowMin) {
          hourMin = windowMin;
        }
      }

      return new Snapshot("min", Math.min(min, hourMin), hourMin, tenMinuteMin, minuteMin);
    }
  }

  public Snapshot getMax() {
    rollWindows();

    synchronized (rollingWindows) {
      int offset = currentOffset + WINDOW_COUNT;
      long hourMax = currentMax.get();
      long minuteMax = hourMax;
      long tenMinuteMax = hourMax;

      // Current second is in currentMax, so start at 1, not 0
      for (int i = 1; i < 3600; ++i) {
        if (i == 60) {
          minuteMax = hourMax;
        } else if (i == 600) {
          tenMinuteMax = hourMax;
        }

        long windowMax = rollingWindows[(offset - i) % WINDOW_COUNT][MAX_INDEX];

        if (hourMax < windowMax) {
          hourMax = windowMax;
        }
      }

      return new Snapshot("max", Math.max(max, hourMax), hourMax, tenMinuteMax, minuteMax);
    }
  }

  private void rollWindows() {
    long now = clock.millis() / 1000;
    long lastRoll = this.lastRoll.get();
    //noinspection NumericCastThatLosesPrecision
    int elapsed = (int) (now - lastRoll);

    if (elapsed >= 1 && this.lastRoll.compareAndSet(lastRoll, now)) {
      long total = this.total.sum();
      long count = this.count.sum();
      long min = currentMin.get();
      long max = currentMax.get();

      if (min < this.min) {
        this.min = min;
      }

      if (max > this.max) {
        this.max = max;
      }

      if (elapsed > WINDOW_COUNT) {
        elapsed = WINDOW_COUNT;
      }

      synchronized (rollingWindows) {
        --elapsed;
        set(rollingWindows[currentOffset], total, count, min, max);

        if (currentOffset >= WINDOW_COUNT - 1) {
          currentOffset = 0;
        } else {
          ++currentOffset;
        }

        while (elapsed > 0) {
          --elapsed;
          set(rollingWindows[currentOffset], total, count, MAX_VALUE, MIN_VALUE);

          if (currentOffset >= WINDOW_COUNT - 1) {
            currentOffset = 0;
          } else {
            ++currentOffset;
          }
        }
      }

      currentMin.compareAndSet(min, MAX_VALUE);
      currentMax.compareAndSet(max, MIN_VALUE);
    }
  }

  private static void set(long[] window, long total, long count, long min, long max) {
    window[TOTAL_INDEX] = total;
    window[COUNT_INDEX] = count;
    window[MIN_INDEX] = min;
    window[MAX_INDEX] = max;
  }

  private static long average(long total, long count) {
    return count <= 0 ? 0 : total / count;
  }
}
