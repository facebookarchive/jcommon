package com.facebook.stats;

import com.google.common.collect.ImmutableList;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.ReadableDateTime;

import java.util.List;
import java.util.Map;

/**
 * Helper methods for converting stat objects into key/value pairs that conform to standing
 * naming conventions around <type>.<time_window>
 */
public class StatsUtil {
  /**
   * @deprecated replaced by addRateAndSumToCounters
   */
  @Deprecated
  public static void adddKeyToCounters(
    String baseKey, ReadableMultiWindowRate rate, Map<String, Long> counterMap
  ) {
    addRateToCounters(baseKey,  rate, counterMap);
    addSumToCounters(baseKey,  rate, counterMap);
  }

  public static void addRateAndSumToCounters(
    String baseKey, ReadableMultiWindowRate rate, Map<String, Long> counterMap
  ) {
    addRateToCounters(baseKey,  rate, counterMap);
    addSumToCounters(baseKey,  rate, counterMap);
  }

  public static void addRateToCounters(
    String baseKey, ReadableMultiWindowRate rate, Map<String, Long> counterMap
  ) {
    counterMap.put(baseKey + ".rate", rate.getAllTimeRate());
    counterMap.put(baseKey + ".rate.3600", rate.getHourRate());
    counterMap.put(baseKey + ".rate.600", rate.getTenMinuteRate());
    counterMap.put(baseKey + ".rate.60", rate.getMinuteRate());
  }

  public static void addSumToCounters(
    String baseKey, ReadableMultiWindowRate rate, Map<String, Long> counterMap
  ) {
    counterMap.put(baseKey + ".sum", rate.getAllTimeSum());
    counterMap.put(baseKey + ".sum.3600", rate.getHourSum());
    counterMap.put(baseKey + ".sum.600", rate.getTenMinuteSum());
    counterMap.put(baseKey + ".sum.60", rate.getMinuteSum());
  }

  public static void addGaugeAvgToCounters(
    String baseKey, ReadableMultiWindowGauge gauge, Map<String, Long> counterMap
  ) {
    counterMap.put(baseKey + ".avg", gauge.getAllTimeAvg());
    counterMap.put(baseKey + ".avg.3600", gauge.getHourAvg());
    counterMap.put(baseKey + ".avg.600", gauge.getTenMinuteAvg());
    counterMap.put(baseKey + ".avg.60", gauge.getMinuteAvg());
  }

  public static void addGaugeSamplesToCounters(
    String baseKey, ReadableMultiWindowGauge gauge, Map<String, Long> counterMap
  ) {
    counterMap.put(baseKey + ".samples", gauge.getAllTimeSamples());
    counterMap.put(baseKey + ".samples.3600", gauge.getHourSamples());
    counterMap.put(baseKey + ".samples.600", gauge.getTenMinuteSamples());
    counterMap.put(baseKey + ".samples.60", gauge.getMinuteSamples());
  }

  /**
   * internal helper method
   *
   * @param baseKey base baseKey name, adds standard time ranges ".60", ".3600", ...
   * @param counter the counter to add
   * @param counterMap map of counters
   */
  private static void addValueToCounters(
    String baseKey, ReadableMultiWindowCounter counter, Map<String, Long> counterMap
  ) {
    counterMap.put(baseKey, counter.getAllTimeValue());
    counterMap.put(baseKey + ".3600", counter.getHourValue());
    counterMap.put(baseKey + ".600", counter.getTenMinuteValue());
    counterMap.put(baseKey + ".60", counter.getMinuteValue());
  }

  public static void addSpreadToCounters(
    String baseKey, MultiWindowSpread spread, Map<String, Long> counterMap
  ) {
    addValueToCounters(baseKey + ".min", spread.getMin(), counterMap);
    addValueToCounters(baseKey + ".max", spread.getMax(), counterMap);
    addGaugeAvgToCounters(baseKey, spread.getGauge(), counterMap);
    addGaugeSamplesToCounters(baseKey, spread.getGauge(), counterMap);
  }

  public static void addQuantileToCounters(
    String baseKey, MultiWindowDistribution quantiles, Map<String, Long> counterMap
  ) {
    addQuantilesToCounters(baseKey, ".60", counterMap, quantiles.getOneMinuteQuantiles());
    addQuantilesToCounters(baseKey, ".600", counterMap, quantiles.getTenMinuteQuantiles());
    addQuantilesToCounters(baseKey, ".3600", counterMap, quantiles.getOneHourQuantiles());
    addQuantilesToCounters(baseKey, "", counterMap, quantiles.getAllTimeQuantiles());
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
    long bucketSize = (max - min + buckets) / buckets;

    ImmutableList.Builder<Long> boundaryBuilder = ImmutableList.builder();
    for (int i = 1; i < buckets + 1; ++i) {
      boundaryBuilder.add(min + bucketSize * i);
    }

    ImmutableList<Long> boundaries = boundaryBuilder.build();
    List<QuantileDigest.Bucket> counts = digest.getHistogram(boundaries);

    StringBuilder builder = new StringBuilder();

    // add bogus bucket (fb303 ui ignores the first one, for whatever reason)
    builder.append("-1:0:0,");

    for (int i = 0; i < boundaries.size(); ++i) {
      long lowBoundary = min;
      if (i > 0) {
        lowBoundary = boundaries.get(i - 1);
      }

      builder.append(lowBoundary)
        .append(':')
        .append(Math.round(counts.get(i).getCount()))
        .append(':')
        .append(Math.round(counts.get(i).getMean()))
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

  public static void main(String[] args) {
    DateTime start = new DateTime("2012-01-01T01:01:00.000Z");
    DateTime end= new DateTime("2012-01-01T01:02:00.000Z");
    Duration x = new Duration(start, end);
    System.err.println(x.getMillis());
  }
}
