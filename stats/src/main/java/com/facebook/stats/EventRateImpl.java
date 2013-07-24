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

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.ReadableDateTime;

public class EventRateImpl implements EventRate {
  private final EventCounterIf<EventCounter> counter;
  private final Duration windowSize;
  private final ReadableDateTime start;

  EventRateImpl(
    EventCounterIf<EventCounter> counter, Duration windowSize, ReadableDateTime start
  ) {
    this.counter = counter;
    this.windowSize = windowSize;
    this.start = start;
  }

  public EventRateImpl(EventCounterIf<EventCounter> counter, Duration windowSize) {
    this(counter, windowSize, new DateTime(DateTimeUtils.currentTimeMillis()));
  }

  @Override
  public void add(long delta) {
    counter.add(delta);
  }

  @Override
  public long getValue() {
    Duration periodSize = getPeriodSize();

    if (periodSize.getStandardSeconds() == 0) {
      return 0;
    }

    return counter.getValue() / periodSize.getStandardSeconds();
  }

  private Duration getPeriodSize() {
    // normalize by the time since server start
    ReadableDateTime now = new DateTime(DateTimeUtils.currentTimeMillis());
    Duration periodSize = new Duration(start, now);

    if (periodSize.isLongerThan(windowSize)) {
      return windowSize;
    } else {
      return periodSize;
    }
  }
}
