package com.facebook.stats;

import org.joda.time.ReadableDateTime;

public class MinEventCounter extends AssociativeAggregationCounter {
  public static final AssociativeAggregation AGGREGATION =
    new AssociativeAggregation() {
      @Override
      public long combine(long l1, long l2) {
        return Math.min(l1, l2);
      }
    };

  public MinEventCounter(
    ReadableDateTime start, ReadableDateTime end, long initialValue
  ) {
    super(start, end, AGGREGATION, initialValue);
  }

  public MinEventCounter(ReadableDateTime start, ReadableDateTime end) {
    this(start, end, Long.MAX_VALUE);
  }
}
