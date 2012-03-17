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
    this(counter, windowSize, new DateTime());
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
    ReadableDateTime now = new DateTime();
    Duration periodSize = new Duration(start, now);

    if (periodSize.isLongerThan(windowSize)) {
      return windowSize;
    } else {
      return periodSize;
    }
  }
}
