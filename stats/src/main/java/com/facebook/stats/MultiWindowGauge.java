package com.facebook.stats;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.joda.time.Duration;
import org.joda.time.ReadableDateTime;

public class MultiWindowGauge implements ReadableMultiWindowGauge, WritableMultiWindowStat {
  private final GaugeCounterFactory gaugeCounterFactory;
  // all-time counter is a windowed counter that is effectively unbounded
  private final CompositeGaugeCounter allTimeCounter;
  private final CompositeGaugeCounter hourCounter;
  private final CompositeGaugeCounter tenMinuteCounter;
  private final CompositeGaugeCounter minuteCounter;
  private volatile GaugeCounter currentCounter;
  private final ReadableDateTime start;

  private final Object rollLock = new Object();


  public MultiWindowGauge() {
    this(DefaultGaugeCounterFactory.INSTANCE);
  }

  public MultiWindowGauge(GaugeCounterFactory gaugeCounterFactory) {
    this(
      gaugeCounterFactory,
      newCompositeGaugeCounter(Integer.MAX_VALUE, gaugeCounterFactory),
      newCompositeGaugeCounter(60, gaugeCounterFactory),
      newCompositeGaugeCounter(10, gaugeCounterFactory),
      newCompositeGaugeCounter(1, gaugeCounterFactory),
      new DateTime()
    );
  }

  MultiWindowGauge(
    GaugeCounterFactory gaugeCounterFactory,
    CompositeGaugeCounter allTimeCounter,
    CompositeGaugeCounter hourCounter,
    CompositeGaugeCounter tenMinuteCounter,
    CompositeGaugeCounter minuteCounter,
    ReadableDateTime start
  ) {
    this.gaugeCounterFactory = gaugeCounterFactory;
    this.allTimeCounter = allTimeCounter;
    this.hourCounter = hourCounter;
    this.tenMinuteCounter = tenMinuteCounter;
    this.minuteCounter = minuteCounter;
    this.start = start;
    currentCounter = nextCurrentCounter();
  }

  private static CompositeGaugeCounter newCompositeGaugeCounter(
    int minutes, GaugeCounterFactory gaugeCounterFactory
  ) {
    return new CompositeGaugeCounter(
      Duration.standardMinutes(minutes), gaugeCounterFactory
    );
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
    if (currentCounter.getEnd().getMillis() < now) {
      synchronized (rollLock) {
        // lock and re-check
        if (currentCounter.getEnd().getMillis() < now) {
          currentCounter = nextCurrentCounter();
        }
      }
    }
  }

  private long calcRate(EventCounterIf<GaugeCounter> counter) {
    long value = counter.getValue();
    ReadableDateTime end = counter.getEnd();
    ReadableDateTime start = counter.getStart();
    ReadableDateTime now = new DateTime();
    Duration duration = now.isBefore(end) ?
      new Duration(start, now) :    // so far
      new Duration(start, end);
    long secs = duration.getStandardSeconds();
    return secs > 0 ? value / secs : value;
  }

  @Override
  public long getMinuteSum() {
    rollCurrentIfNeeded();
    return minuteCounter.getValue();
  }

  @Override
  public long getMinuteSamples() {
    rollCurrentIfNeeded();
    return minuteCounter.getSamples();
  }

  @Override
  public long getMinuteAvg() {
    rollCurrentIfNeeded();
    return minuteCounter.getAverage();
  }

  @Override
  public long getMinuteRate() {
    rollCurrentIfNeeded();
    return calcRate(minuteCounter);
  }

  @Override
  public long getTenMinuteSum() {
    rollCurrentIfNeeded();
    return tenMinuteCounter.getValue();
  }

  @Override
  public long getTenMinuteSamples() {
    rollCurrentIfNeeded();
    return tenMinuteCounter.getSamples();
  }

  @Override
  public long getTenMinuteAvg() {
    rollCurrentIfNeeded();
    return tenMinuteCounter.getAverage();
  }

  @Override
  public long getTenMinuteRate() {
    rollCurrentIfNeeded();
    return calcRate(tenMinuteCounter);
  }

  @Override
  public long getHourSum() {
    rollCurrentIfNeeded();
    return hourCounter.getValue();
  }

  @Override
  public long getHourSamples() {
    rollCurrentIfNeeded();
    return hourCounter.getSamples();
  }

  @Override
  public long getHourAvg() {
    rollCurrentIfNeeded();
    return hourCounter.getAverage();
  }

  @Override
  public long getHourRate() {
    rollCurrentIfNeeded();
    return calcRate(hourCounter);
  }

  @Override
  public long getAllTimeSum() {
    return allTimeCounter.getValue();
  }

  @Override
  public long getAllTimeSamples() {
    return allTimeCounter.getSamples();
  }

  @Override
  public long getAllTimeAvg() {
    return allTimeCounter.getAverage();
  }

  @Override
  public long getAllTimeRate() {
    Duration sinceStart = new Duration(start, new DateTime());
    if (sinceStart.getStandardSeconds() == 0) {
      return 0;
    }
    return allTimeCounter.getValue() / sinceStart.getStandardSeconds();
  }

  /*
   * Create a new counter for the next 6 secs and add it to all the
   * composite counters.
   */
  private GaugeCounter nextCurrentCounter() {
    ReadableDateTime now = new DateTime();
    GaugeCounter gaugeCounter = gaugeCounterFactory.create(
      now, now.toDateTime().plusSeconds(6)
    );

    allTimeCounter.addEventCounter(gaugeCounter);
    hourCounter.addEventCounter(gaugeCounter);
    tenMinuteCounter.addEventCounter(gaugeCounter);
    minuteCounter.addEventCounter(gaugeCounter);

    return gaugeCounter;
  }

  public MultiWindowGauge merge(MultiWindowGauge rhs) {
    return new MultiWindowGauge(
      gaugeCounterFactory,
      (CompositeGaugeCounter)allTimeCounter.merge(rhs.allTimeCounter),
      (CompositeGaugeCounter)hourCounter.merge(rhs.hourCounter),
      (CompositeGaugeCounter)tenMinuteCounter.merge(rhs.tenMinuteCounter),
      (CompositeGaugeCounter)minuteCounter.merge(rhs.minuteCounter),
      start.isBefore(rhs.start) ? start : rhs.start
    );
  }
}
