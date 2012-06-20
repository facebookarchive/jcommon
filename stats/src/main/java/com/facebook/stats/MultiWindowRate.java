package com.facebook.stats;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.joda.time.Duration;
import org.joda.time.ReadableDateTime;

public class MultiWindowRate implements ReadableMultiWindowRate {
  // all-time counter is a windowed counter that is effectively unbounded
  protected final CompositeSum allTimeCounter;
  private final CompositeSum hourCounter;
  private final CompositeSum tenMinuteCounter;
  private final CompositeSum minuteCounter;
  private volatile EventCounterIf<EventCounter> currentCounter;
  private final EventRate hourRate;
  private final EventRate tenMinuteRate;
  private final EventRate minuteRate;
  private final ReadableDateTime start;
  private final Object rollLock = new Object();

  public MultiWindowRate() {
    allTimeCounter = newCompositeEventCounter(Integer.MAX_VALUE);
    hourCounter = newCompositeEventCounter(60);
    tenMinuteCounter = newCompositeEventCounter(10);
    minuteCounter = newCompositeEventCounter(1);
    start = getNow();
    hourRate =
      newEventRate(hourCounter, Duration.standardMinutes(60), start);
    tenMinuteRate =
      newEventRate(tenMinuteCounter, Duration.standardMinutes(10), start);
    minuteRate =
      newEventRate(minuteCounter, Duration.standardMinutes(1), start);
    currentCounter = nextCurrentCounter(start.toDateTime());
  }

  MultiWindowRate(
    CompositeSum allTimeCounter,
    CompositeSum hourCounter,
    CompositeSum tenMinuteCounter,
    CompositeSum minuteCounter,
    ReadableDateTime start
  ) {
    this.allTimeCounter = allTimeCounter;
    this.hourCounter = hourCounter;
    this.tenMinuteCounter = tenMinuteCounter;
    this.minuteCounter = minuteCounter;
    this.start = start;
    hourRate =
      newEventRate(hourCounter, Duration.standardMinutes(60), start);
    tenMinuteRate =
      newEventRate(tenMinuteCounter, Duration.standardMinutes(10), start);
    minuteRate =
      newEventRate(minuteCounter, Duration.standardMinutes(1), start);
    currentCounter = nextCurrentCounter(start.toDateTime());
  }

  private CompositeSum newCompositeEventCounter(int minutes) {
    return new CompositeSum(Duration.standardMinutes(minutes));
  }

  private EventRate newEventRate(
    EventCounterIf<EventCounter> counter, Duration windowSize, ReadableDateTime start
  ) {
    return new EventRateImpl(counter, windowSize, start);
  }

  public void add(long delta) {
    rollCurrentIfNeeded();
    currentCounter.add(delta);
  }

  private void rollCurrentIfNeeded() {
    //do outside the synchronized block
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
  private EventCounterIf<EventCounter> nextCurrentCounter(DateTime now) {
    SumEventCounter sumEventCounter = new SumEventCounter(
      now, now.plusSeconds(6)
    );

    allTimeCounter.addEventCounter(sumEventCounter);
    hourCounter.addEventCounter(sumEventCounter);
    tenMinuteCounter.addEventCounter(sumEventCounter);
    minuteCounter.addEventCounter(sumEventCounter);

    return sumEventCounter;
  }

  public MultiWindowRate merge(MultiWindowRate rate) {
    return new MultiWindowRate(
      (CompositeSum)allTimeCounter.merge(rate.allTimeCounter),
      (CompositeSum)hourCounter.merge(rate.hourCounter),
      (CompositeSum)tenMinuteCounter.merge(rate.tenMinuteCounter),
      (CompositeSum)minuteCounter.merge(rate.minuteCounter),
      start.isBefore(rate.start) ? start : rate.start
    );
  }
}
