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
