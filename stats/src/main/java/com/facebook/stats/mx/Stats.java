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
package com.facebook.stats.mx;


import com.facebook.logging.Logger;
import com.facebook.logging.LoggerImpl;
import com.facebook.stats.MultiWindowDistribution;
import com.facebook.stats.MultiWindowRate;
import com.facebook.stats.MultiWindowSpread;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

public class Stats implements StatsReader, StatsCollector {
  private static final Logger LOG = LoggerImpl.getLogger(Stats.class);
  private static final String ERROR_FLAG = "--ERROR--";

  private final String prefix;
  private final ConcurrentMap<String, Callable<String>> attributes = new ConcurrentHashMap<>();
  // generic counters; anything here will have sum/rate for 1m/10m/60m/all-time
  private final ConcurrentMap<String, MultiWindowRate> rates = new ConcurrentHashMap<>();
  private final ConcurrentMap<String, AtomicLong> counters = new ConcurrentHashMap<>();
  private final ConcurrentMap<String, Callable<Long>> dynamicCounters = new ConcurrentHashMap<>();
  private final ConcurrentMap<String, MultiWindowSpread> spreads = new ConcurrentHashMap<>();
  private final ConcurrentMap<String, MultiWindowDistribution> distributions =
    new ConcurrentHashMap<>();

  public Stats(String prefix) {
    this.prefix = prefix;
  }

  public Stats() {
    this("");
  }

  private MultiWindowRate getMultiWindowRate(
    String key, ConcurrentMap<String, MultiWindowRate> map
  ) {
    MultiWindowRate rate = map.get(key);

    if (rate == null) {
      rate = new MultiWindowRate();
      MultiWindowRate existingRate = map.putIfAbsent(key, rate);

      if (existingRate != null) {
        rate = existingRate;
      }
    }

    return rate;
  }

  @Override
  public void exportCounters(Map<String, Long> counterMap) {
    for (Map.Entry<String, MultiWindowRate> entry : rates.entrySet()) {
      StatsUtil.addRateAndSumToCounters(
        prefix + entry.getKey(), entry.getValue(), counterMap
      );
    }

    for (Map.Entry<String, MultiWindowSpread> entry : spreads.entrySet()) {
      StatsUtil.addSpreadToCounters(
        prefix + entry.getKey(), entry.getValue(), counterMap
      );
    }

    for (Map.Entry<String, MultiWindowDistribution> entry : distributions.entrySet()) {
      StatsUtil.addQuantileToCounters(
        prefix + entry.getKey(), entry.getValue(), counterMap
      );
    }

    Long duplicate = null;
    for (Map.Entry<String, AtomicLong> entry : counters.entrySet()) {
      duplicate = counterMap.put(prefix + entry.getKey(), entry.getValue().get());
      if (duplicate != null) {
        LOG.warn("Duplicate counter : %s, Ignoring old value %d", duplicate);
      }
    }

    for (Map.Entry<String, Callable<Long>> entry : dynamicCounters.entrySet()) {
      try {
        duplicate = counterMap.put(prefix + entry.getKey(), entry.getValue().call());
        if (duplicate != null) {
          LOG.warn("Duplicate counter : %s, Ignoring old value %d", duplicate);
        }
      } catch (Exception e) {
        LOG.debug(e, "Exception when generating dynamic counter value for %s", entry.getKey());
      }
    }
  }

  @Override
  public MultiWindowRate getRate(StatType statType) {
    return getRate(statType.getKey());
  }

  @Override
  public MultiWindowRate getRate(String key) {
    return getMultiWindowRate(key, rates);
  }


  @Override
  public void incrementRate(StatType type, long delta) {
    getMultiWindowRate(type.getKey(), rates).add(delta);
  }

  @Override
  public void incrementRate(String key, long delta) {
    getMultiWindowRate(key, rates).add(delta);
  }

  @Override
  public void incrementCounter(StatType key, long delta) {
    internalIncrementCounter(key.getKey(), delta);
  }

  @Override
  public void incrementCounter(String key, long delta) {
    internalIncrementCounter(key, delta);
  }

  private void internalIncrementCounter(String key, long delta) {
    AtomicLong value = counters.get(key);

    if (value == null) {
      value = new AtomicLong(0);
      AtomicLong existingValue = counters.putIfAbsent(key, value);

      if (existingValue != null) {
        value = existingValue;
      }
    }

    value.addAndGet(delta);
  }

  @Override
  @Deprecated
  public long setCounter(StatType statType, long value) {
    return StatsUtil.setCounterValue(statType.getKey(), value, this);
  }

  @Override
  @Deprecated
  public long setCounter(String key, long value) {
    return StatsUtil.setCounterValue(key, value, this);
  }

  public long resetCounter(StatType key) {
    return internalResetCounter(key.getKey());
  }

  @Override
  public long resetCounter(String key) {
    return internalResetCounter(key);
  }

  private long internalResetCounter(String key) {
    AtomicLong value = counters.put(key, new AtomicLong(0));

    return value == null ? 0 : value.get();
  }

  public void incrementSpread(StatType type, long value) {
    getMultiWindowSpread(type.getKey()).add(value);  }

  @Override
  public void incrementSpread(String key, long value) {
    getMultiWindowSpread(key).add(value);
  }

  @Override
  public void updateDistribution(StatType type, long value) {
    getMultiWindowDistribution(type.getKey()).add(value);
  }

  @Override
  public void updateDistribution(String key, long value) {
    getMultiWindowDistribution(key).add(value);
  }

  @Override
  public long getCounter(StatType key) {
    return internalGetCounter(key.getKey());
  }

  @Override
  public long getCounter(String key) {
    return internalGetCounter(key);
  }

  @Override
  public MultiWindowSpread getSpread(StatType key) {
    return getMultiWindowSpread(key.getKey());
  }

  @Override
  public MultiWindowSpread getSpread(String key) {
    return getMultiWindowSpread(key);
  }

  @Override
  public MultiWindowDistribution getDistribution(StatType key) {
    return getMultiWindowDistribution(key.getKey());
  }

  @Override
  public MultiWindowDistribution getDistribution(String key) {
    return getMultiWindowDistribution(key);
  }

  /**
   * Sets the dynamic counter if a counter with the specified key doesn't already exist
   *
   * @param key the key for the dynamic counter
   * @param valueProducer the generator value for this counter
   *
   * @return true if the counter was added. False, if a counter with the specified key exists
   * already
   */
  public boolean addDynamicCounter(String key, Callable<Long> valueProducer) {
    return null == dynamicCounters.putIfAbsent(key, valueProducer);
  }

  /**
   * Removes a dynamic counter with the specified key
   *
   * @param key the key for the dynamic counter
   * @return true if a counter with the specified key existed and was removed, false otherwise.
   */
  public boolean removeDynamicCounter(String key) {
    return dynamicCounters.remove(key) != null;
  }

  private long internalGetCounter(String key) {
    AtomicLong value = counters.get(key);

    return value == null ? 0 : value.get();
  }

  @Override
  public String getAttribute(StatType key) {
    return internalGetAttribute(key.getKey());
  }

  @Override
  public void setAttribute(StatType key, String value) {
    internalSetAttribute(key.getKey(), new StringProducer(value));
  }

  @Override
  public void setAttribute(String key, String value) {
    internalSetAttribute(key, new StringProducer(value));
  }

  @Override
  public void setAttribute(StatType key, Callable<String> valueProducer) {
    internalSetAttribute(key.getKey(), valueProducer);
  }

  @Override
  public void setAttribute(String key, Callable<String> valueProducer) {
    internalSetAttribute(key, valueProducer);
  }

  private void internalSetAttribute(String key, Callable<String> valueProducer) {
    try {
      attributes.put(key, valueProducer);
    } catch (Exception e) {
      LOG.error(e, "error in producer for key %s", key);
    }
  }

  @Override
  public String getAttribute(String key) {
    return internalGetAttribute(key);
  }

  private String internalGetAttribute(String key) {
    try {
      Callable<String> callable = attributes.get(key);

      return callable == null ? null : callable.call();
    } catch (Exception e) {
      LOG.error(e, "error producing value for key %s", key);
      return ERROR_FLAG;
    }
  }

  @Override
  public Map<String, String> getAttributes() {
    return materializeAttributes();
  }

  private Map<String, String> materializeAttributes() {
    Map<String, String> materializedAttributes = new HashMap<String, String>();

    for (Map.Entry<String, Callable<String>> entry : attributes.entrySet()) {
      try {
        materializedAttributes.put(entry.getKey(), entry.getValue().call());
      } catch (Exception e) {
        materializedAttributes.put(entry.getKey(), ERROR_FLAG);
        LOG.error(e, "error producing value for key %s", entry.getKey());
      }
    }

    for (Map.Entry<String, MultiWindowDistribution> entry : distributions.entrySet()) {
      StatsUtil.addHistogramToExportedValues(
        entry.getKey(), entry.getValue(), materializedAttributes
      );
    }

    return materializedAttributes;
  }

  private MultiWindowSpread getMultiWindowSpread(String key) {
    MultiWindowSpread spread = spreads.get(key);

    if (spread == null) {
      spread = new MultiWindowSpread();
      MultiWindowSpread existingSpreads = spreads.putIfAbsent(key, spread);

      if (existingSpreads != null) {
        spread = existingSpreads;
      }
    }

    return spread;
  }

  private MultiWindowDistribution getMultiWindowDistribution(String key) {
    MultiWindowDistribution distribution = distributions.get(key);

    if (distribution == null) {
      distribution = new MultiWindowDistribution();
      MultiWindowDistribution existing = distributions.putIfAbsent(key, distribution);

      if (existing != null) {
        distribution = existing;
      }
    }

    return distribution;
  }



  private static class StringProducer implements Callable<String> {
    private final String value;

    private StringProducer(String value) {
      this.value = value;
    }

    @Override
    public String call() throws Exception {
      return value;
    }
  }
}
