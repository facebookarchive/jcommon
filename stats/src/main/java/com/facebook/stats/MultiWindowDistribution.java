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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.facebook.stats.MultiWindowDistribution.Quantile.*;
import static com.google.common.collect.Lists.transform;

public class MultiWindowDistribution implements WritableMultiWindowStat {
  private final QuantileDigest oneMinute;
  private final QuantileDigest tenMinutes;
  private final QuantileDigest oneHour;
  private final QuantileDigest allTime;

  @VisibleForTesting
  MultiWindowDistribution(
    QuantileDigest oneMinute,
    QuantileDigest tenMinutes,
    QuantileDigest oneHour,
    QuantileDigest allTime
  ) {
    this.oneMinute = oneMinute;
    this.tenMinutes = tenMinutes;
    this.oneHour = oneHour;
    this.allTime = allTime;
  }

  public MultiWindowDistribution() {
    this(
      new QuantileDigest(0.01, ExponentialDecay.computeAlpha(0.1, 60)),
      new QuantileDigest(0.01, ExponentialDecay.computeAlpha(0.1, 600)),
      new QuantileDigest(0.01, ExponentialDecay.computeAlpha(0.1, 3600)),
      new QuantileDigest(0.01)
    );
  }

  @Override
  public void add(long value) {
    oneMinute.add(value);
    tenMinutes.add(value);
    oneHour.add(value);
    allTime.add(value);
  }

  public QuantileDigest getOneMinute() {
    return oneMinute;
  }

  public QuantileDigest getTenMinutes() {
    return tenMinutes;
  }

  public QuantileDigest getOneHour() {
    return oneHour;
  }

  public QuantileDigest getAllTime() {
    return allTime;
  }

  public Map<Quantile, Long> getOneMinuteQuantiles() {
    return getQuantiles(oneMinute);
  }

  public Map<Quantile, Long> getTenMinuteQuantiles() {
    return getQuantiles(tenMinutes);
  }

  public Map<Quantile, Long> getOneHourQuantiles() {
    return getQuantiles(oneHour);
  }

  public Map<Quantile, Long> getAllTimeQuantiles() {
    return getQuantiles(allTime);
  }

  private Map<Quantile, Long> getQuantiles(QuantileDigest digest) {
    List<Quantile> keys = ImmutableList.of(P50, P75, P95, P99);
    List<Long> values = digest.getQuantiles(transform(keys, getQuantileFunction()));

    Iterator<Quantile> keyIterator = keys.iterator();
    Iterator<Long> valueIterator = values.iterator();

    EnumMap<Quantile, Long> result = new EnumMap<>(Quantile.class);
    while (keyIterator.hasNext() && valueIterator.hasNext()) {
      result.put(keyIterator.next(), valueIterator.next());
    }

    return result;
  }

  public enum Quantile {
    P50("p50", 0.5),
    P75("p75", 0.75),
    P95("p95", 0.95),
    P99("p99", 0.99);

    private final String key;
    private final double quantile;

    private Quantile(String key, double quantile) {
      this.key = key;
      this.quantile = quantile;
    }

    public String getKey() {
      return key;
    }

    public double getQuantile() {
      return quantile;
    }

    public static Function<Quantile, Double> getQuantileFunction() {
      return new Function<Quantile, Double>() {
        public Double apply(Quantile input) {
          return input.getQuantile();
        }
      };
    }
  }
}
