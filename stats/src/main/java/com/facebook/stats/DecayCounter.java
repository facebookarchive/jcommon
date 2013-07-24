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

import java.util.concurrent.atomic.AtomicLong;

/**
 * special purpose counter that decays exponentially fashion.
 * <p/>
 * Note: all value is decayed as though it was added at the 'start' value.
 * This is best used to decay the counter value once it has been fixed
 */
public class DecayCounter implements EventCounter {
  private final AtomicLong count = new AtomicLong(0);
  private final ReadableDateTime start;
  private final ReadableDateTime decayStart;
  private final ReadableDateTime end;
  private final float decayRatePerSecond;

  /**
   * @param start              start of the counter range
   * @param decayStart         time from which to compute decay
   * @param end                end of the counter range
   * @param decayRatePerSecond ex, 0.05 => 5% decay per second
   */
  public DecayCounter(
    ReadableDateTime start,
    ReadableDateTime decayStart,
    ReadableDateTime end, float decayRatePerSecond
  ) {
    this.start = start;
    this.decayStart = decayStart;
    this.end = end;
    this.decayRatePerSecond = decayRatePerSecond;
  }

  public DecayCounter(
    ReadableDateTime start, ReadableDateTime end, float decayRatePerSecond
  ) {
    this(start, start, end, decayRatePerSecond);
  }

  @Override
  public void add(long delta) {
    count.addAndGet(delta);
  }

  /**
   * @return counter value after computing exponential decay of the counter
   *         value
   */
  @Override
  public long getValue() {
    DateTime now = getNow();

    // don't start decay unless it's at least 1s after the decayStart
    if (now.isAfter(decayStart.toDateTime().plusSeconds(1))) {
      Duration elapsed = new Duration(decayStart, now);
      long millis = elapsed.getMillis();

      // compute total decay for millis / 1000 seconds
      double thisDecay =
        Math.pow(1.0 - decayRatePerSecond, (double) (millis / (double) 1000));

      return (long) (count.get() * thisDecay);
    } else {
      return count.get();
    }
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

  /**
   * creates a merged counter with our decayed value + the other counter's
   * value.  This will be a DecayCounter with a decayStart of getNow()
   * and a range that spans the extend of both
   *
   * @param counter : any EventCounter
   * @return
   */
  @Override
  public EventCounter merge(EventCounter counter) {
    ReadableDateTime mergedStart =
      start.isBefore(counter.getStart()) ? start : counter.getStart();
    ReadableDateTime mergedEnd =
      end.isAfter(counter.getEnd()) ? end : counter.getEnd();
    DateTime now = getNow();
    DecayCounter mergedCounter = new DecayCounter(
      mergedStart,
      now.isAfter(decayStart) ? now : decayStart,
      mergedEnd,
      decayRatePerSecond
    );

    mergedCounter.add(getValue());
    mergedCounter.add(counter.getValue());

    return mergedCounter;
  }

  DateTime getNow() {
    return new DateTime(DateTimeUtils.currentTimeMillis());
  }
}

