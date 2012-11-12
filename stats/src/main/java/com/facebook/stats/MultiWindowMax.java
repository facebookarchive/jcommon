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
import org.joda.time.DateTimeUtils;
import org.joda.time.Duration;
import org.joda.time.ReadableDateTime;
import org.joda.time.ReadableDuration;

public class MultiWindowMax implements ReadableMultiWindowCounter, WritableMultiWindowStat {
  private static final ReadableDuration COUNTER_GRANULARITY =
    Duration.standardSeconds(6);

  private final Object rollLock = new Object();
  private final CompositeMax allTimeCounter;
  private final CompositeMax hourCounter;
  private final CompositeMax tenMinuteCounter;
  private final CompositeMax minuteCounter;
  private volatile EventCounterIf<EventCounter> currentCounter;

  MultiWindowMax(
    CompositeMax allTimeCounter,
    CompositeMax hourCounter,
    CompositeMax tenMinuteCounter,
    CompositeMax minuteCounter
  ) {
    this.allTimeCounter = allTimeCounter;
    this.hourCounter = hourCounter;
    this.tenMinuteCounter = tenMinuteCounter;
    this.minuteCounter = minuteCounter;
    currentCounter = addNewCurrentCounter();
  }

  public MultiWindowMax() {
    this(
      new CompositeMax(Duration.standardMinutes(Integer.MAX_VALUE)),
      new CompositeMax(Duration.standardMinutes(60)),
      new CompositeMax(Duration.standardMinutes(10)),
      new CompositeMax(Duration.standardMinutes(1))
    );
  }

  @Override
  public void add(long value) {
    rollCurrentIfNeeded();
    currentCounter.add(value);
  }

  @Override
  public long getMinuteValue() {
    rollCurrentIfNeeded();
    return minuteCounter.getValue();
  }

  @Override
  public long getTenMinuteValue() {
    rollCurrentIfNeeded();
    return tenMinuteCounter.getValue();
  }

  @Override
  public long getHourValue() {
    rollCurrentIfNeeded();
    return hourCounter.getValue();
  }

  @Override
  public long getAllTimeValue() {
    rollCurrentIfNeeded();
    return allTimeCounter.getValue();
  }

  private void rollCurrentIfNeeded() {
    //do outside the synchronized block
    long now = DateTimeUtils.currentTimeMillis();
    // this is false for the majority of calls, so skip lock acquisition
    if (currentCounter.getEnd().getMillis() < now) {
      synchronized (rollLock) {
        // lock and re-check
        if (currentCounter.getEnd().getMillis() < now) {
          currentCounter = addNewCurrentCounter();
        }
      }
    }
  }

  private MaxEventCounter addNewCurrentCounter() {
    ReadableDateTime now = new DateTime();

    MaxEventCounter maxEventCounter = new MaxEventCounter(
      now,
      now.toDateTime().plus(COUNTER_GRANULARITY)
    );

    allTimeCounter.addEventCounter(maxEventCounter);
    hourCounter.addEventCounter(maxEventCounter);
    tenMinuteCounter.addEventCounter(maxEventCounter);
    minuteCounter.addEventCounter(maxEventCounter);

    return maxEventCounter;
  }

  public MultiWindowMax merge(MultiWindowMax other) {
    return new MultiWindowMax(
      (CompositeMax) allTimeCounter.merge(other.allTimeCounter),
      (CompositeMax) hourCounter.merge(other.hourCounter),
      (CompositeMax) tenMinuteCounter.merge(other.tenMinuteCounter),
      (CompositeMax) minuteCounter.merge(other.minuteCounter)
    );
  }
}
