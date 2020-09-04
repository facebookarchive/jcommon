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
import org.joda.time.Duration;
import org.joda.time.ReadableDateTime;

/**
 * Fast implementation of GaugeCounter that may give slightly inaccurate values in certain race
 * conditions, but should produce close to accurate results in the long run.
 */
public class FastGaugeCounter implements GaugeCounter {
  private final ReadableDateTime start;
  private final ReadableDateTime end;
  private final AtomicLong value = new AtomicLong(0);
  private final AtomicLong nsamples = new AtomicLong(0);

  public FastGaugeCounter(ReadableDateTime start, ReadableDateTime end) {
    if (start.isAfter(end)) {
      this.start = end;
      this.end = start;
    } else {
      this.start = start;
      this.end = end;
    }
  }

  @Override
  public void add(long delta, long samples) {
    value.addAndGet(delta);
    nsamples.addAndGet(samples);
  }

  @Override
  public void add(long delta) {
    add(delta, 1);
  }

  @Override
  public long getValue() {
    return value.get();
  }

  @Override
  public long getSamples() {
    return nsamples.get();
  }

  @Override
  public long getAverage() {
    long samples = nsamples.get();
    if (samples == 0) {
      return 0;
    }
    return value.get() / samples;
  }

  @Override
  public ReadableDateTime getStart() {
    return start;
  }

  @Override
  public ReadableDateTime getEnd() {
    return end;
  }

  @Override
  public Duration getLength() {
    return new Duration(start, end);
  }

  @Override
  public GaugeCounter merge(GaugeCounter counter) {
    ReadableDateTime mergedStart = start;
    ReadableDateTime mergedEnd = end;

    if (counter.getStart().isBefore(mergedStart)) {
      mergedStart = counter.getStart();
    }

    if (counter.getEnd().isAfter(mergedEnd)) {
      mergedEnd = counter.getEnd();
    }

    DefaultGaugeCounter mergedCounter = new DefaultGaugeCounter(mergedStart, mergedEnd);

    mergedCounter.add(value.get() + counter.getValue(), nsamples.get() + counter.getSamples());

    return mergedCounter;
  }
}
