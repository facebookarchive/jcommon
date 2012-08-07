package com.facebook.stats;

public interface WritableMultiWindowStat {
  /**
   * @param value value to add to the underlying multi-window statistic
   */
  public void add(long value);
}
