package com.facebook.stats.mx;


import com.facebook.stats.MultiWindowRate;

import java.util.Map;

public interface StatsReader {
  public void exportCounters(Map<String, Long> counters);
  public MultiWindowRate getRate(String key);
  public MultiWindowRate getRate(StatType statType);
  public long getCounter(StatType key);
  public long getCounter(String key);
  public String getAttribute(StatType key);
  public String getAttribute(String key);

  /**
   * @return returns a snapshot copy of the attributes
   */
  public Map<String, String> getAttributes();
}

