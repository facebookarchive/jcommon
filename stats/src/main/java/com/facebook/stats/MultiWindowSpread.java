/*
 * Copyright (C) 2012 Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.stats;

/**
 * Stats container that tracks the following multi-window metrics:
 * 1) min
 * 2) gauge - average, rate, sample count
 * 3) max
 */
public class MultiWindowSpread implements WritableMultiWindowStat {
  private final MultiWindowMin min;
  private final MultiWindowGauge gauge;
  private final MultiWindowMax max;

  public MultiWindowSpread(LongCounterFactory longCounterFactory) {
    this(new DefaultGaugeCounterFactory(longCounterFactory));
  }

  public MultiWindowSpread() {
    this(AtomicLongCounter::new);
  }

  public MultiWindowSpread(GaugeCounterFactory gaugeCounterFactory) {
    min = new MultiWindowMin();
    gauge = new MultiWindowGauge(gaugeCounterFactory);
    max = new MultiWindowMax();
  }

  @Override
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
