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
 * reference implementation of a simple counter for a bounded period of time
 */
public class EventCounterImpl implements EventCounter {
  private final ReadableDateTime start;
  private final ReadableDateTime end;
  private final LongCounter value;
  private final LongCounterFactory longCounterFactory;

  public EventCounterImpl(
    ReadableDateTime start,
    ReadableDateTime end,
    long initialValue,
    LongCounterFactory longCounterFactory
  ) {
    this.longCounterFactory = longCounterFactory;
    if (start.isAfter(end)) {
      this.start = end;
      this.end = start;
    } else {
      this.start = start;
      this.end = end;
    }

    this.value = longCounterFactory.create(initialValue);
  }

  public EventCounterImpl(
    ReadableDateTime start,
    ReadableDateTime end,
    long initialValue
  ) {
    this(start, end, initialValue, AtomicLongCounter::new);
  }

  public EventCounterImpl(
    ReadableDateTime start,
    ReadableDateTime end,
    LongCounterFactory longCounterFactory
  ) {
    this(start, end, 0, longCounterFactory);
  }

  public EventCounterImpl(
    ReadableDateTime start,
    ReadableDateTime end
  ) {
    this(start, end, AtomicLongCounter::new);
  }

  public static EventCounterImpl create(ReadableDateTime start, ReadableDateTime end) {
    return new EventCounterImpl(start, end, 0, AtomicLongCounter::new);
  }

  public static EventCounterImpl createAdder(ReadableDateTime start, ReadableDateTime end) {
    return new EventCounterImpl(start, end, 0, LongAdderCounter::new);
  }

  public void add(long delta) {
    value.update(delta);
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

    EventCounterImpl mergedCounter = new EventCounterImpl(mergedStart, mergedEnd, 0, longCounterFactory);

    mergedCounter.add(value.get() + counter.getValue());

    return mergedCounter;
  }
}
