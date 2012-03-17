package com.facebook.stats;

import org.joda.time.ReadableDateTime;

/**
 * Factory interface for creating new GaugeCounters
 */
public interface GaugeCounterFactory {
  public GaugeCounter create(ReadableDateTime start, ReadableDateTime end);
}
