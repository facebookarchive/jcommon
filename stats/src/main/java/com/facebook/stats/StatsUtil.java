package com.facebook.stats;

import com.google.common.collect.ImmutableList;
import org.joda.time.Duration;
import org.joda.time.ReadableDateTime;

import java.util.List;
import java.util.Map;

public class StatsUtil {
  public static void addKeyToCounters(
    String baseKey, ReadableMultiWindowRate rate, Map<String, Long> counters
  ) {
    counters.put(baseKey + ".rate", rate.getAllTimeRate());
    counters.put(baseKey + ".rate.3600", rate.getHourRate());
    counters.put(baseKey + ".rate.600", rate.getTenMinuteRate());
    counters.put(baseKey + ".rate.60", rate.getMinuteRate());
    counters.put(baseKey + ".sum", rate.getAllTimeSum());
    counters.put(baseKey + ".sum.3600", rate.getHourSum());
    counters.put(baseKey + ".sum.600", rate.getTenMinuteSum());
    counters.put(baseKey + ".sum.60", rate.getMinuteSum());
  }

  public static void addGaugeAvgToCounters(
    String baseKey, ReadableMultiWindowGauge gauge, Map<String, Long> counters
  ) {
    counters.put(baseKey + ".avg", gauge.getAllTimeAvg());
    counters.put(baseKey + ".avg.3600", gauge.getHourAvg());
    counters.put(baseKey + ".avg.600", gauge.getTenMinuteAvg());
    counters.put(baseKey + ".avg.60", gauge.getMinuteAvg());
  }

  public static void addGaugeSamplesToCounters(
    String baseKey, ReadableMultiWindowGauge gauge, Map<String, Long> counters
  ) {
    counters.put(baseKey + ".samples", gauge.getAllTimeSamples());
    counters.put(baseKey + ".samples.3600", gauge.getHourSamples());
    counters.put(baseKey + ".samples.600", gauge.getTenMinuteSamples());
    counters.put(baseKey + ".samples.60", gauge.getMinuteSamples());
  }

  public static void addKeyToCounters(
    String baseKey,
    String aggName,
    ReadableMultiWindowCounter counter,
    Map<String, Long> counters
  ) {
    counters.put(baseKey + "." + aggName, counter.getAllTimeValue());
    counters.put(baseKey + "." + aggName + ".3600", counter.getHourValue());
    counters.put(baseKey + "." + aggName + ".600", counter.getTenMinuteValue());
    counters.put(baseKey + "." + aggName + ".60", counter.getMinuteValue());
  }

  public static void addSpreadToCounters(
    String baseKey, MultiWindowSpread spread, Map<String, Long> counters
  ) {
    addKeyToCounters(baseKey, "min", spread.getMin(), counters);
    addKeyToCounters(baseKey, "max", spread.getMax(), counters);
    addGaugeAvgToCounters(baseKey, spread.getGauge(), counters);
    addGaugeSamplesToCounters(baseKey, spread.getGauge(), counters);
  }

  public static void addQuantileToCounters(
    String baseKey, MultiWindowDistribution quantiles, Map<String, Long> counters
  ) {
    addQuantilesToCounters(baseKey, ".60", counters, quantiles.getOneMinuteQuantiles());
    addQuantilesToCounters(baseKey, ".600", counters, quantiles.getTenMinuteQuantiles());
    addQuantilesToCounters(baseKey, ".3600", counters, quantiles.getOneHourQuantiles());
    addQuantilesToCounters(baseKey, "", counters, quantiles.getAllTimeQuantiles());
  }

  public static void addHistogramToExportedValues(
    String baseKey, MultiWindowDistribution quantiles, Map<String, String> values
  ) {
    addHistogramToExportedValues(baseKey, ".60", values, quantiles.getOneMinute());
    addHistogramToExportedValues(baseKey, ".600", values, quantiles.getTenMinutes());
    addHistogramToExportedValues(baseKey, ".3600", values, quantiles.getOneHour());
    addHistogramToExportedValues(baseKey, "", values, quantiles.getAllTime());
  }

  private static void addHistogramToExportedValues(
    String baseKey, String windowKey, Map<String, String> values, QuantileDigest digest
  ) {
    values.put(baseKey + ".hist" + windowKey, serializeHistogram(digest));
  }

  private static String serializeHistogram(QuantileDigest digest) {
    int buckets = 100;

    long min = digest.getMin();
    long max = digest.getMax();
    long bucketSize = (max - min) / buckets;

    ImmutableList.Builder<Long> boundaryBuilder = ImmutableList.builder();
    for (int i = 1; i < buckets + 1; ++i) {
      boundaryBuilder.add(min + bucketSize * i);
    }

    ImmutableList<Long> boundaries = boundaryBuilder.build();
    List<QuantileDigest.Bucket> counts = digest.getHistogram(boundaries);

    StringBuilder builder = new StringBuilder();

    // add bogus bucket (fb303 ui ignores the first one, for whatever reason)
    builder.append("0:0:0,");
    for (int i = 1; i < boundaries.size(); ++i) {
      builder.append(boundaries.get(i - 1))
        .append(':')
        .append((long) counts.get(i).getCount())
        .append(':')
        .append((long) counts.get(i).getMean())
        .append(',');
    }

    // add a final bucket so that fb303 ui shows the max value
    builder.append(max);
    builder.append(":0:0");

    return builder.toString();
  }

  private static void addQuantilesToCounters(
    String baseKey,
    String windowKey,
    Map<String, Long> counters,
    Map<MultiWindowDistribution.Quantile, Long> oneMinuteQuantiles
  ) {
    for (Map.Entry<MultiWindowDistribution.Quantile, Long> entry : oneMinuteQuantiles.entrySet()) {
      counters.put(baseKey + "." + entry.getKey().getKey() + windowKey, entry.getValue());
    }
  }

  public static void setAllTimeSum(MultiWindowRate rate, long value) {
    long old = rate.getAllTimeSum();
    if(old > value) {
      throw new IllegalArgumentException("MultiWindowRate counters can not decrement their allTimeSum (" + old + " > " + value + ")");
    }
    if(old == value) {
      return;
    }
    long delta = value - old;
    rate.add(delta);
  }

  public static Duration extentOf(
    EventCounterIf counter1, EventCounterIf counter2
  ) {
    ReadableDateTime start = counter1.getStart();
    ReadableDateTime end = counter1.getEnd();

    if (counter2.getStart().isBefore(start)) {
      start = counter2.getStart();
    }

    if (counter2.getEnd().isAfter(end)) {
      end = counter2.getEnd();
    }

    return new Duration(start, end);
  }
}
