package com.facebook.stats;

import org.joda.time.Duration;
import org.joda.time.ReadableDateTime;

/**
 * counter object representing some value over a timespan [start, end)
 *
 * mutable via the add() metthod
 * @param <C>
 */
public interface EventCounterIf<C extends EventCounterIf<C>> {
  public void add(long delta);
  public long getValue();
  public ReadableDateTime getStart();
  public ReadableDateTime getEnd();
  public Duration getLength();

  /**
   * Produces a merged counter that spans the range of both counters
   * and contains the sum value.
   *
   * guarantees that the underlying implementation will be the same as the LHS
   * (Composite, single)
   *
   *
   * @param counter : counter to merge with. Merge implementation behavior is not defined
   *                if the contents of the input counter are modified; the resulting counter
   *                returned may be invalid or a ConcurrentModificationException may be thrown,
   *                depending.
   *
   * @return new EventCounter resulting from merging this and counter
   */
  public C merge(C counter);
}
