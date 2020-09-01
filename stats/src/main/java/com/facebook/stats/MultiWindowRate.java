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

public class MultiWindowRate implements ReadableMultiWindowRate, WritableMultiWindowStat {
  private static final int DEFAULT_TIME_BUCKET_SIZE_MILLIS = 6000; // 6 seconds
  // all-time counter is a windowed counter that is effectively unbounded
  private final CompositeSum allTimeCounter;
  private final CompositeSum hourCounter;
  private final CompositeSum tenMinuteCounter;
  private final CompositeSum minuteCounter;
  private final EventRate hourRate;
  private final EventRate tenMinuteRate;
  private final EventRate minuteRate;
  private final ReadableDateTime start;
  private final Object rollLock = new Object();
  private final int timeBucketSizeMillis;

  private volatile EventCounterIf<EventCounter> currentCounter;

  MultiWindowRate(int timeBucketSizeMillis) {
    this(
        newCompositeEventCounter(Integer.MAX_VALUE),
        newCompositeEventCounter(60),
        newCompositeEventCounter(10),
        newCompositeEventCounter(1),
        new DateTime(),
        timeBucketSizeMillis);
  }

  MultiWindowRate(
      CompositeSum allTimeCounter,
      CompositeSum hourCounter,
      CompositeSum tenMinuteCounter,
      CompositeSum minuteCounter,
      ReadableDateTime start,
      int timeBucketSizeMillis) {
    this.allTimeCounter = allTimeCounter;
    this.hourCounter = hourCounter;
    this.tenMinuteCounter = tenMinuteCounter;
    this.minuteCounter = minuteCounter;
    this.start = start;
    this.timeBucketSizeMillis = timeBucketSizeMillis;
    hourRate = newEventRate(hourCounter, Duration.standardMinutes(60), start);
    tenMinuteRate = newEventRate(tenMinuteCounter, Duration.standardMinutes(10), start);
    minuteRate = newEventRate(minuteCounter, Duration.standardMinutes(1), start);
    currentCounter = nextCurrentCounter(start.toDateTime());
  }

  public MultiWindowRate() {
    this(DEFAULT_TIME_BUCKET_SIZE_MILLIS);
  }

  private static CompositeSum newCompositeEventCounter(int minutes) {
    return new CompositeSum(Duration.standardMinutes(minutes));
  }

  private EventRate newEventRate(
      EventCounterIf<EventCounter> counter, Duration windowSize, ReadableDateTime start) {
    return new EventRateImpl(counter, windowSize, start);
  }

  @Override
  public void add(long delta) {
    rollCurrentIfNeeded();
    currentCounter.add(delta);
  }

  private void rollCurrentIfNeeded() {
    // do outside the synchronized block
    long now = DateTimeUtils.currentTimeMillis();
    // this is false for the majority of calls, so skip lock acquisition
    if (currentCounter.getEnd().getMillis() <= now) {
      synchronized (rollLock) {
        // lock and re-check
        if (currentCounter.getEnd().getMillis() <= now) {
          currentCounter = nextCurrentCounter(new DateTime(now));
        }
      }
    }
  }

  @Override
  public long getMinuteSum() {
    rollCurrentIfNeeded();

    return minuteCounter.getValue();
  }

  @Override
  public long getMinuteRate() {
    rollCurrentIfNeeded();

    return minuteRate.getValue();
  }

  @Override
  public long getTenMinuteSum() {
    rollCurrentIfNeeded();

    return tenMinuteCounter.getValue();
  }

  @Override
  public long getTenMinuteRate() {
    rollCurrentIfNeeded();

    return tenMinuteRate.getValue();
  }

  @Override
  public long getHourSum() {
    rollCurrentIfNeeded();

    return hourCounter.getValue();
  }

  @Override
  public long getHourRate() {
    rollCurrentIfNeeded();

    return hourRate.getValue();
  }

  @Override
  public long getAllTimeSum() {
    return allTimeCounter.getValue();
  }

  @Override
  public long getAllTimeRate() {
    Duration sinceStart = new Duration(start, getNow());

    if (sinceStart.getStandardSeconds() == 0) {
      return 0;
    }

    return allTimeCounter.getValue() / sinceStart.getStandardSeconds();
  }

  protected ReadableDateTime getNow() {
    return new DateTime();
  }

  // current
  private EventCounterIf<EventCounter> nextCurrentCounter(ReadableDateTime now) {
    EventCounter eventCounter =
        new EventCounterImpl(now, now.toDateTime().plusMillis(timeBucketSizeMillis));

    allTimeCounter.addEventCounter(eventCounter);
    hourCounter.addEventCounter(eventCounter);
    tenMinuteCounter.addEventCounter(eventCounter);
    minuteCounter.addEventCounter(eventCounter);

    return eventCounter;
  }

  public MultiWindowRate merge(MultiWindowRate rate) {
    return new MultiWindowRate(
        (CompositeSum) allTimeCounter.merge(rate.allTimeCounter),
        (CompositeSum) hourCounter.merge(rate.hourCounter),
        (CompositeSum) tenMinuteCounter.merge(rate.tenMinuteCounter),
        (CompositeSum) minuteCounter.merge(rate.minuteCounter),
        start.isBefore(rate.start) ? start : rate.start,
        timeBucketSizeMillis);
  }
}
