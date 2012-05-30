package com.facebook.stats.mx;


import com.facebook.stats.MultiWindowDistribution;
import com.facebook.stats.MultiWindowRate;
import com.facebook.stats.MultiWindowSpread;

import java.util.Map;

public interface StatsReader {
  public void exportCounters(Map<String, Long> counters);
  public MultiWindowRate getRate(String key);
  public MultiWindowRate getRate(StatType statType);
  public long getCounter(StatType key);
  public long getCounter(String key);
  public MultiWindowSpread getSpread(StatType key);
  public MultiWindowSpread getSpread(String key);
  public MultiWindowDistribution getDistribution(StatType key);
  public MultiWindowDistribution getDistribution(String key);
  public String getAttribute(StatType key);
  public String getAttribute(String key);

  /**
   * @return returns a snapshot copy of the attributes
   */
  public Map<String, String> getAttributes();
}

