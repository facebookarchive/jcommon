package com.facebook.stats;

import org.joda.time.ReadableDateTime;

public interface EventCounterIf<C extends EventCounterIf<C>> {
  public void add(long delta);
  public long getValue();
  public ReadableDateTime getStart();
  public ReadableDateTime getEnd();

  /**
   * Produces a merged counter that spans the range of both counters
   * and contains the sum value.
   *
   * guarantees
   *  1. underlying implementation will be the same as the LHS
   * (Composite, single)
   *  2. LHS and RHS remain unchanged
   *
   * @param counter :
   * @return new EventCounter resulting from merging this and counter
   */
  public C merge(C counter);
}
