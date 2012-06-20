package com.facebook.stats;

import org.joda.time.ReadableDateTime;

public interface CompositeEventCounterIf<C extends EventCounterIf<C>> extends EventCounterIf<C> {
  /**
   * creates a new counter and adds it to the composite one. Despite being a public method, this is
   * most often used inside of other counters to add a new "head" counter when computing multiple
   * window lengths. The counter object is shared by all windows and eventually merged when its
   * duration has completed
   *
   * @param delta value of the new counter
   * @param start
   * @param end
   * @return the CompositeEventCounterIf after adding the new counter
   */
  public CompositeEventCounterIf<C> add(
    long delta, ReadableDateTime start, ReadableDateTime end
  );

  /**
   * Add an already created counter to the ComopsiteEvenCountIf. This is similar to
   * {@link #add(long, org.joda.time.ReadableDateTime, org.joda.time.ReadableDateTime)} but
   * externalizes creation of the counter to add.  ie, you can add another CompositeEventCounterIf
   * if C so allows it, allowing for nesting of composition to arbitrary depths.
   *
   * @param eventCounter counter to add
   * @return this after adding eventCounter
   */
  public CompositeEventCounterIf<C> addEventCounter(C eventCounter);
}
