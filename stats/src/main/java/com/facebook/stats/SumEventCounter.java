package com.facebook.stats;

import org.joda.time.ReadableDateTime;

/**
 * A bit of a hack for performance, but we can improve override the add()
 * method call to get a bit more performance.
 *
 * @deprecated see original simple implementation, {@link EventCounterImpl} which
 * does the same thing, but avoids encapsulation violation
 */
@Deprecated
public class SumEventCounter extends AssociativeAggregationCounter {
  public static final AssociativeAggregation AGGREGATION =
    new AssociativeAggregation() {
      @Override
      public long combine(long l1, long l2) {
        return l1 + l2;
      }
    };

  public SumEventCounter(
    ReadableDateTime start, ReadableDateTime end, long initialValue
  ) {
    super(start, end, AGGREGATION, initialValue);
  }

  public SumEventCounter(ReadableDateTime start, ReadableDateTime end) {
    this(start, end, 0L);
  }

  @Override
  public void add(long delta) {
    value.addAndGet(delta);
  }
}
