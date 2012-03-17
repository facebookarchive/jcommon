package com.facebook.stats;

import org.joda.time.ReadableDateTime;

public class FastGaugeCounterFactory implements GaugeCounterFactory {
  public static GaugeCounterFactory INSTANCE = new FastGaugeCounterFactory();

  @Override
  public GaugeCounter create(ReadableDateTime start, ReadableDateTime end) {
    return new FastGaugeCounter(start, end);
  }
}
