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

import org.joda.time.Duration;
import org.joda.time.ReadableDateTime;

/**
 * Default implementation of GaugeCounter. For a slightly faster, but less
 * accurate version, see FastGaugeCounter.
 */
public class DefaultGaugeCounter implements GaugeCounter {
  private final ReadableDateTime start;
  private final ReadableDateTime end;
  private long value = 0;
  private long nsamples = 0;

  public DefaultGaugeCounter(ReadableDateTime start, ReadableDateTime end) {
    if (start.isAfter(end)) {
      this.start = end;
      this.end = start;
    } else {
      this.start = start;
      this.end = end;
    }
  }

  @Override
  public synchronized void add(long delta, long samples) {
    value += delta;
    nsamples += samples;
  }

  @Override
  public synchronized void add(long delta) {
    add(delta, 1);
  }

  @Override
  public synchronized long getValue() {
    return value;
  }

  @Override
  public synchronized long getSamples() {
    return nsamples;
  }

  @Override
  public synchronized long getAverage() {
    if (nsamples == 0) {
      return 0;
    }
    return value / nsamples;
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

  public GaugeCounter merge(GaugeCounter counter) {
    ReadableDateTime mergedStart = start;
    ReadableDateTime mergedEnd = end;

    if (counter.getStart().isBefore(mergedStart)) {
      mergedStart = counter.getStart();
    }

    if (counter.getEnd().isAfter(mergedEnd)) {
      mergedEnd = counter.getEnd();
    }

    DefaultGaugeCounter mergedCounter =
      new DefaultGaugeCounter(mergedStart, mergedEnd);

      mergedCounter.add(
        value + counter.getValue(),
        nsamples + ((GaugeCounter)counter).getSamples()
      );

    return mergedCounter;
  }
}
