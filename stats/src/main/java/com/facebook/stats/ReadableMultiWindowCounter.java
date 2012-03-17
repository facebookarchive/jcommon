package com.facebook.stats;

public interface ReadableMultiWindowCounter {
  public long getMinuteValue();
  public long getTenMinuteValue();
  public long getHourValue();
  public long getAllTimeValue();
}
