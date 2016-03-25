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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.facebook.logging.Logger;
import com.facebook.logging.LoggerImpl;

/**
 * This was written to resemble some older libraries.  You may find
 * more functionality in StatsUtil.java.
 *
 * See com.facebook.fb303.stats.HistoryManager for more info on how
 * this works.
 *
 * The parameters "shortName" refer to a stat name such as "queries"
 * or "cpu_load", and "fullName" refer to the longer
 * "queries.sum.600", "cpu_load.avg.3600".
 */

/**
 * Each stat's name corresponds to a single underlying counter, but
 * each counter can be exported for different uses (ExportTypes). For
 * details see com.facebook.fb303.stats.HistoryManager.
 */
public class StatsManager implements HistoryManager {
  private static Logger logger = LoggerImpl.getLogger(StatsManager.class);

  private ConcurrentHashMap<String, Integer> typeMap;
  // todo: handle mutliple shortName/types
  private ConcurrentHashMap<String, MultiWindowGauge> counterMap;

  /**
   * Creates a StatsMgr instance with default settings for the internal
   * ConcurrentHashMaps.
   */
  public StatsManager() {
    this(16, 0.75f, 16);
  }

  /**
   * Creates a StatsMgr instance with the specified initial capacity and
   * concurrency level for the ConcurrentHashMaps used internally.
   *
   * @param initialNumKeys:  expected initial number of keys
   * @param loadFactor:  load factor of the internal hash maps
   * @param concurrencyLevel:  estimated number of concurrently updating threads
   */
  public StatsManager(int initialNumKeys,
                      float loadFactor,
                      int concurrencyLevel) 
  {
    logger.trace("StatsMgr Created");
    this.typeMap =
      new ConcurrentHashMap<String, Integer>(initialNumKeys,
                                             loadFactor,
                                             concurrencyLevel);
    this.counterMap =
      new ConcurrentHashMap<String, MultiWindowGauge>(initialNumKeys,
                                                      loadFactor,
                                                      concurrencyLevel);
  }

  private void ensureStat(String shortName) {
    if (counterMap.containsKey(shortName)) {
      return;
    }
    MultiWindowGauge mwg = new MultiWindowGauge();
    MultiWindowGauge wasGauge = counterMap.putIfAbsent(shortName, mwg);
    if (wasGauge == null) {
      if (logger.isDebugEnabled()) {
        logger.debug("Created stat " + shortName);
      }
    } else {
      if (logger.isTraceEnabled()) {
        logger.trace("almost accidentally created stat" + shortName
                + " twice.  phew...");
      }
    }
  }

  private void ensureType(String shortName, ExportType etype) {
    boolean hasType = typeMap.containsKey(shortName);
    if (!hasType) {
      Integer prior = typeMap.putIfAbsent(shortName, etype.value());
      if (prior == null) {
        return;
      }
      // some other thread got there first
    }
    Integer bitmask = ExportType.NONE.value();
    boolean done;
    Integer newValue;
    int tries = 0;
    do {
      bitmask = typeMap.get(shortName);
      newValue = bitmask | etype.value();
      if (bitmask == newValue) {
        break;
      }
      done = typeMap.replace(shortName, bitmask, newValue);
      tries++;
    } while (!done);

    if (logger.isTraceEnabled()) {
      logger.trace("Updated type for " + shortName + ", added " + etype
          + ", was " + bitmask + ", now " + newValue + " (after " + tries
          + " tries)");
    }
    return;
  }

  public void addStatExportType(String shortName, ExportType etype) {
    if (shortName == null) {
      logger.error("Null value passed as key");
      return;
    }
    if (etype == null) {
      logger.error("Null value passed as exportType");
      return;
    }
    ensureStat(shortName);
    ensureType(shortName, etype);
  }

  public void addStatValue(String shortName, long delta) {
    if (!counterMap.containsKey(shortName)) {
      addStatExportType(shortName, ExportType.AVG);
    }
    MultiWindowGauge stat = counterMap.get(shortName);
    stat.add(delta);
  }

  // fb303
  public long getCounter(String fullName) {
    // todo: convert to a more intelligent table/map version
    int lastDot = fullName.lastIndexOf('.');
    if (lastDot <= 0) {
      throw new IllegalArgumentException("Stat name argument '" + fullName
          + "' not found");
    }

    int preLastDot = -1;
    String shortName = null;
    String ending = null;
    String ending2 = null;
    preLastDot = fullName.lastIndexOf('.', lastDot - 1);
    ending = fullName.substring(lastDot);

    if (preLastDot > 0) {
      shortName = fullName.substring(0, preLastDot);
      ending2 = fullName.substring(preLastDot);
    } else {
      shortName = fullName.substring(0, lastDot);
    }

    try {
      if (ending.equals(".60")) {
        if (ending2.equals(".sum.60")) {
          return counterMap.get(shortName).getMinuteSum();
        }
        if (ending2.equals(".avg.60")) {
          return counterMap.get(shortName).getMinuteAvg();
        }
        if (ending2.equals(".rate.60")) {
          return counterMap.get(shortName).getMinuteRate();
        }
        if (ending2.equals(".count.60")) {
          return counterMap.get(shortName).getMinuteSamples();
        }
      }

      if (ending.equals(".600")) {
        if (ending2.equals(".sum.600")) {
          return counterMap.get(shortName).getTenMinuteSum();
        }
        if (ending2.equals(".avg.600")) {
          return counterMap.get(shortName).getTenMinuteAvg();
        }
        if (ending2.equals(".rate.600")) {
          return counterMap.get(shortName).getTenMinuteRate();
        }
        if (ending2.equals(".count.600")) {
          return counterMap.get(shortName).getTenMinuteSamples();
        }
      }

      if (ending.equals(".3600")) {
        if (ending2.equals(".sum.3600")) {
          return counterMap.get(shortName).getHourSum();
        }
        if (ending2.equals(".avg.3600")) {
          return counterMap.get(shortName).getHourAvg();
        }
        if (ending2.equals(".rate.3600")) {
          return counterMap.get(shortName).getHourRate();
        }
        if (ending2.equals(".count.3600")) {
          return counterMap.get(shortName).getHourSamples();
        }
      }

      if (fullName.endsWith(".sum")) {
        return counterMap.get(shortName).getAllTimeSum();
      }
      if (fullName.endsWith(".avg")) {
        return counterMap.get(shortName).getAllTimeAvg();
      }
      if (fullName.endsWith(".rate")) {
        return counterMap.get(shortName).getAllTimeRate();
      }
      if (fullName.endsWith(".count")) {
        return counterMap.get(shortName).getAllTimeSamples();
      }
    } catch (Exception e) {
      throw new IllegalArgumentException("Stat name '" + shortName
          + "' not found for '" + ending2 + "' or '" + ending + "'");
    }

    throw new IllegalArgumentException("Stat name argument '" + fullName
        + "' not found");
  }

  /**
   * Returns a new map of results and any missing keys will be missing
   * from output map.
   *
   * fb303-support
   */
  public Map<String, Long> getSelectedCounters(List<String> keys) {
    Map<String, Long> result = new HashMap<String, Long>();
    for (String key : keys) {
      try {
        result.put(key, getCounter(key));
      } catch (IllegalArgumentException e) {
        // okay, result will be missing for this
      }
    }
    return result;
  }

  /**
   * fb303-support
   *
   * @param fullName:  key with the .sum.60 parts, etc
   */
  public boolean hasCounter(String fullName) {
    try {
      getCounter(fullName);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  /**
   * Returns true if the shortName has been added already.
   *
   * @param shortName:  The stat name used with addStatExportType does
   *          NOT have the .sum.60 parts, etc
   */
  public boolean containsKey(String shortName) {
    return counterMap.containsKey(shortName);
  }

  /**
   * fb303-support
   */
  public Map<String, Long> getCounters() 
  {
    Map<String, Long> result = new HashMap<String, Long>();
    String fullname;
    long value;
    Set<String> typeKeys = typeMap.keySet();
    for (String name : typeKeys) {
      int bitmask = typeMap.get(name);
      MultiWindowGauge stat = counterMap.get(name);
      for (ExportType type : ExportType.values()) {
        if ((bitmask & type.value()) == 0) {
          continue;
        }
        // minute
        fullname = String.format("%s.%s.60", name, type);
        switch (type) {
        case SUM:
          value = stat.getMinuteSum();
          break;
        case COUNT:
          value = stat.getMinuteSum();
          break;
        case AVG:
          value = stat.getMinuteAvg();
          break;
        case RATE:
          value = stat.getMinuteRate();
          break;
        default:
          throw new IndexOutOfBoundsException("Bad type");
        }
        result.put(fullname, value);

        // 10 min
        fullname = String.format("%s.%s.600", name, type);
        switch (type) {
        case SUM:
          value = stat.getTenMinuteSum();
          break;
        case COUNT:
          value = stat.getTenMinuteSum();
          break;
        case AVG:
          value = stat.getTenMinuteAvg();
          break;
        case RATE:
          value = stat.getTenMinuteRate();
          break;
        default:
          throw new IndexOutOfBoundsException("Bad type");
        }
        result.put(fullname, value);

        // hour
        fullname = String.format("%s.%s.3600", name, type);
        switch (type) {
        case SUM:
          value = stat.getHourSum();
          break;
        case COUNT:
          value = stat.getHourSum();
          break;
        case AVG:
          value = stat.getHourAvg();
          break;
        case RATE:
          value = stat.getHourRate();
          break;
        default:
          throw new IndexOutOfBoundsException("Bad type");
        }
        result.put(fullname, value);

        // all time
        fullname = String.format("%s.%s", name, type);
        switch (type) {
        case SUM:
          value = stat.getAllTimeSum();
          break;
        case COUNT:
          value = stat.getAllTimeSum();
          break;
        case AVG:
          value = stat.getAllTimeAvg();
          break;
        case RATE:
          value = stat.getAllTimeRate();
          break;
        default:
          throw new IndexOutOfBoundsException("Bad type");
        }
        result.put(fullname, value);

      } // for all export types
    } // for all window stat names
    return result;
  }

}
