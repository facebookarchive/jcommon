package com.facebook.stats.mx;

import java.util.concurrent.Callable;

public interface StatsCollector {
  public void incrementRate(StatType type, long delta);
  public void incrementRate(String key, long delta);
  public void incrementCounter(StatType key, long delta);
  public void incrementCounter(String key, long delta);
  public void setAttribute(StatType key, String value);
  public void setAttribute(String key, String value);
  public void setAttribute(StatType key, Callable<String> valueProducer);
  public void setAttribute(String key, Callable<String> valueProducer);
}

