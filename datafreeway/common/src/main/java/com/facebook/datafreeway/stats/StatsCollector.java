package com.facebook.datafreeway.stats;

import java.util.concurrent.Callable;

public interface StatsCollector {
  public void incrementRate(StatType type, long delta);
  public void incrementRate(String key, long delta);
  public void incrementCounter(StatType key, long delta);
  public void incrementCounter(String key, long delta);
  public void incrementSpread(StatType type, long value);
  public void incrementSpread(String key, long value);
  public void setAttribute(StatType key, String value);
  public void setAttribute(String key, String value);
  public void setAttribute(StatType key, Callable<String> valueProducer);
  public void setAttribute(String key, Callable<String> valueProducer);
}

