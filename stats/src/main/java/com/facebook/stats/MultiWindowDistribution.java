package com.facebook.stats;

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

  public MultiWindowDistribution() {
    oneMinute = new QuantileDigest(0.01, ExponentialDecay.computeAlpha(0.1, 60));
    tenMinutes = new QuantileDigest(0.01, ExponentialDecay.computeAlpha(0.1, 600));
    oneHour = new QuantileDigest(0.01, ExponentialDecay.computeAlpha(0.1, 3600));
    allTime = new QuantileDigest(0.01);
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

    EnumMap<Quantile, Long> result = new EnumMap<Quantile, Long>(Quantile.class);
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
