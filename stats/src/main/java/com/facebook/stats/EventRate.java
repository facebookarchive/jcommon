package com.facebook.stats;

public interface EventRate {
  public void add(long delta);
  public long getValue();
}
