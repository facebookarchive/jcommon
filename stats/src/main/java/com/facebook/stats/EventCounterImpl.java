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
import java.util.concurrent.atomic.AtomicLong;

/**
 * reference implementation of a simple counter for a bounded period of time
 */
public class EventCounterImpl implements EventCounter {
  private final ReadableDateTime start;
  private final ReadableDateTime end;
  private final AtomicLong value;

  public EventCounterImpl(ReadableDateTime start, ReadableDateTime end, long initialValue) {
    if (start.isAfter(end)) {
      this.start = end;
      this.end = start;
    } else {
      this.start = start;
      this.end = end;
    }

    this.value = new AtomicLong(initialValue);
  }

  public EventCounterImpl(ReadableDateTime start, ReadableDateTime end) {
    this(start, end, 0L);
  }

  public void add(long delta) {
    value.addAndGet(delta);
  }

  public long getValue() {
    return value.get();
  }

  public ReadableDateTime getStart() {
    return start;
  }

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

    EventCounterImpl mergedCounter = new EventCounterImpl(mergedStart, mergedEnd);

    mergedCounter.add(value.get() + counter.getValue());

    return mergedCounter;
  }
}
