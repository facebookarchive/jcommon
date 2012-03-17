package com.facebook.stats;

/**
 * Like an EventCounter but with additional functionality and
 * additional overhead.
 */
public interface GaugeCounter extends EventCounterIf<GaugeCounter> {
  /**
   * Number of times add(long delta) has be called.
   */
  public long getSamples();

  /**
   * Average value of delta for calls to add(long delta).
   */
  public long getAverage();

  /**
   * Like add(delta) but used for merging with other GaugeCounters.
   */
  public void add(long delta, long nsamples);
}
