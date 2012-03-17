package com.facebook.stats;

import java.util.Map;

/**
 * A interface for reporting statistics (counters, exported values)
 */
public interface FacebookStatsReporter {

  void deleteCounter(String key);

  void resetCounter(String key);

  long incrementCounter(String key);

  long incrementCounter(String key, long increment);

  long setCounter(String key, long value);

  void setExportedValue(String key, String value);

  void removeExportedValue(String key);

  Map<String, Long> makeCounters();
}

