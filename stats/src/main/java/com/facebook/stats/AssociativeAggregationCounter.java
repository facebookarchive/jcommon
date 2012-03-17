package com.facebook.stats;

import org.joda.time.ReadableDateTime;

import java.util.concurrent.atomic.AtomicLong;

public class AssociativeAggregationCounter implements EventCounter {
  private final ReadableDateTime start;
  private final ReadableDateTime end;
  private final long initialValue;
  private final AssociativeAggregation associativeAggregation;
  protected final AtomicLong value;

  public AssociativeAggregationCounter(
    ReadableDateTime start,
    ReadableDateTime end,
    AssociativeAggregation associativeAggregation,
    long initialValue
  ) {
    if (start.isAfter(end)) {
      this.start = end;
      this.end = start;
    } else {
      this.start = start;
      this.end = end;
    }
    this.initialValue = initialValue;
    this.associativeAggregation = associativeAggregation;
    this.value = new AtomicLong(initialValue);
  }

  public void add(long delta) {
    long val = value.get();
    while (
      !value.compareAndSet(val, associativeAggregation.combine(val, delta))
    ) {
      val = value.get();
    }
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
  public EventCounter merge(EventCounter counter) {
    ReadableDateTime mergedStart = start;
    ReadableDateTime mergedEnd = end;

    if (counter.getStart().isBefore(mergedStart)) {
      mergedStart = counter.getStart();
    }

    if (counter.getEnd().isAfter(mergedEnd)) {
      mergedEnd = counter.getEnd();
    }

    AssociativeAggregationCounter mergedCounter =
      new AssociativeAggregationCounter(
        mergedStart, mergedEnd, associativeAggregation, initialValue
      );

    mergedCounter.add(
      associativeAggregation.combine(value.get(), counter.getValue())
    );

    return mergedCounter;
  }
}
