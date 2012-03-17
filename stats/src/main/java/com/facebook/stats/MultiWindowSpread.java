package com.facebook.stats;

/**
 * Stats container that tracks the following multi-window metrics:
 * 1) min
 * 2) gauge - average, rate, sample count
 * 3) max
 */
public class MultiWindowSpread {
  private final MultiWindowMin min;
  private final MultiWindowGauge gauge;
  private final MultiWindowMax max;

  public MultiWindowSpread() {
    this(DefaultGaugeCounterFactory.INSTANCE);
  }

  public MultiWindowSpread(GaugeCounterFactory gaugeCounterFactory) {
    min = new MultiWindowMin();
    gauge = new MultiWindowGauge(gaugeCounterFactory);
    max = new MultiWindowMax();
  }

  public void add(long value) {
    min.add(value);
    gauge.add(value);
    max.add(value);
  }

  public ReadableMultiWindowCounter getMin() {
    return min;
  }

  public ReadableMultiWindowGauge getGauge() {
    return gauge;
  }

  public ReadableMultiWindowCounter getMax() {
    return max;
  }
}
