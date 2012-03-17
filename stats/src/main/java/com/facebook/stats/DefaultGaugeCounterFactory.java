package com.facebook.stats;

import org.joda.time.ReadableDateTime;

public class DefaultGaugeCounterFactory implements GaugeCounterFactory {
  public static GaugeCounterFactory INSTANCE = new DefaultGaugeCounterFactory();

  @Override
  public GaugeCounter create(ReadableDateTime start, ReadableDateTime end) {
    return new DefaultGaugeCounter(start, end);
  }
}
