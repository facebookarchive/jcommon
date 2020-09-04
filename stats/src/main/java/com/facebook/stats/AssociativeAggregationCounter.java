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

public class AssociativeAggregationCounter implements EventCounter {
  private final ReadableDateTime start;
  private final ReadableDateTime end;
  private final long initialValue;
  private final AssociativeAggregation associativeAggregation;
  protected final AtomicLong value;

  public AssociativeAggregationCounter(
      ReadableDateTime start,
      ReadableDateTime end,
      AssociativeAggregation associativeAggregation,
      long initialValue) {
    if (start.isAfter(end)) {
      this.start = end;
      this.end = start;
    } else {
      this.start = start;
      this.end = end;
    }
    this.initialValue = initialValue;
    this.associativeAggregation = associativeAggregation;
    this.value = new AtomicLong(initialValue);
  }

  @Override
  public void add(long delta) {
    long val = value.get();
    while (!value.compareAndSet(val, associativeAggregation.combine(val, delta))) {
      val = value.get();
    }
  }

  @Override
  public long getValue() {
    return value.get();
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
  public EventCounter merge(EventCounter counter) {
    ReadableDateTime mergedStart = start;
    ReadableDateTime mergedEnd = end;

    if (counter.getStart().isBefore(mergedStart)) {
      mergedStart = counter.getStart();
    }

    if (counter.getEnd().isAfter(mergedEnd)) {
      mergedEnd = counter.getEnd();
    }

    AssociativeAggregationCounter mergedCounter =
        new AssociativeAggregationCounter(
            mergedStart, mergedEnd, associativeAggregation, initialValue);

    mergedCounter.add(associativeAggregation.combine(value.get(), counter.getValue()));

    return mergedCounter;
  }
}
