package com.facebook.stats;

import org.joda.time.Duration;
import org.joda.time.ReadableDateTime;

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
