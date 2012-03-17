package com.facebook.stats;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.IllegalArgumentException;

import org.apache.log4j.Logger;


/**
 * See com.facebook.fb303.stats.HistoryManager for more info.
 *
 * The parameters "shortName" refer to a stat name such as "queries"
 * or "cpu_load", and "fullName" refer to the longer
 * "queries.sum.600", "cpu_load.avg.3600".
 */

/**
 * Each stat's name corresponds to a single underlying counter, but
 * each counter can be exported for different uses (ExportTypes).  For
 * details see com.facebook.fb303.stats.HistoryManager.
 */
public class StatsManager implements HistoryManager {
  private static Logger logger_ = Logger.getLogger(StatsManager.class);

  private ConcurrentHashMap<String, Integer> typeMap_;
  // todo: handle mutliple shortName/types
  private ConcurrentHashMap<String, MultiWindowGauge> counterMap_;

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
   * @param initialNumKeys    expected initial number of keys
   * @param loadFactor        load factor of the internal hash maps
   * @param concurrencyLevel  estimated number of concurrently updating threads
   */
  public StatsManager(int initialNumKeys, float loadFactor, int concurrencyLevel) {
    logger_.trace("StatsMgr Created");
    this.typeMap_ = new ConcurrentHashMap<String, Integer>(
        initialNumKeys,
        loadFactor,
        concurrencyLevel);
    this.counterMap_ = new ConcurrentHashMap<String, MultiWindowGauge>(
        initialNumKeys,
        loadFactor,
        concurrencyLevel);
  }

  private void ensureStat(String shortName) {
    if (counterMap_.containsKey(shortName)) {
      return;
    }
    MultiWindowGauge mwg = new MultiWindowGauge();
    MultiWindowGauge wasGauge = counterMap_.putIfAbsent(shortName, mwg);
    if (wasGauge == null) {
      if (logger_.isDebugEnabled()) {
        logger_.debug("Created stat " + shortName);
      }
    } else {
      if (logger_.isTraceEnabled()) {
        logger_.trace("almost accidentally created stat" + shortName +
                      " twice.  phew...");
      }
    }
  }

  private void ensureType(String shortName, ExportType etype) {
    boolean hasType = typeMap_.containsKey(shortName);
    if (!hasType) {
      Integer prior = typeMap_.putIfAbsent(shortName, etype.value());
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
      bitmask = typeMap_.get(shortName);
      newValue = bitmask | etype.value();
      if (bitmask == newValue) {
        break;
      }
      done = typeMap_.replace(shortName, bitmask, newValue);
      tries++;
    } while(!done);

    if (logger_.isTraceEnabled()) {
      logger_.trace("Updated type for " + shortName +
                    ", added " + etype +
                    ", was " + bitmask +
                    ", now " + newValue +
                    " (after " + tries + " tries)");
    }
    return;
  }

  public void addStatExportType(String shortName, ExportType etype) {
    if (shortName == null) {
      logger_.error("Null value passed as key");
      return;
    }
    if (etype == null) {
      logger_.error("Null value passed as exportType");
      return;
    }
    ensureStat(shortName);
    ensureType(shortName, etype);
  }

  public void addStatValue(String shortName, long delta) {
    if (!counterMap_.containsKey(shortName)) {
      addStatExportType(shortName, ExportType.AVG);
    }
    MultiWindowGauge stat = counterMap_.get(shortName);
    stat.add(delta);
  }

  // fb303
  public long getCounter(String fullName) {
    // todo: convert to a more intelligent table/map version
    int lastDot = fullName.lastIndexOf('.');
    int preLastDot = -1;
    String shortName = null;
    String ending = null;
    String ending2 = null;

    if (lastDot > 0) {
      preLastDot = fullName.lastIndexOf('.', lastDot-1);
      ending = fullName.substring(lastDot);
    }

    if (preLastDot > 0) {
      shortName = fullName.substring(0, preLastDot);
      ending2 = fullName.substring(preLastDot);
    } else {
      shortName = fullName.substring(0, lastDot);
    }

    try {
      if (ending.equals(".60")) {
        if (ending2.equals(".sum.60")) {
          return counterMap_.get(shortName).getMinuteSum();
        }
        if (ending2.equals(".avg.60")) {
          return counterMap_.get(shortName).getMinuteAvg();
        }
        if (ending2.equals(".rate.60")) {
          return counterMap_.get(shortName).getMinuteRate();
        }
        if (ending2.equals(".count.60")) {
          return counterMap_.get(shortName).getMinuteSum();
        }
      }

      if (ending.equals(".600")) {
        if (ending2.equals(".sum.600")) {
          return counterMap_.get(shortName).getTenMinuteSum();
        }
        if (ending2.equals(".avg.600")) {
          return counterMap_.get(shortName).getTenMinuteAvg();
        }
        if (ending2.equals(".rate.600")) {
          return counterMap_.get(shortName).getTenMinuteRate();
        }
        if (ending2.equals(".count.600")) {
          return counterMap_.get(shortName).getTenMinuteSum();
        }
      }

      if (ending.equals(".3600")) {
        if (ending2.equals(".sum.3600")) {
          return counterMap_.get(shortName).getHourSum();
        }
        if (ending2.equals(".avg.3600")) {
          return counterMap_.get(shortName).getHourAvg();
        }
        if (ending2.equals(".rate.3600")) {
          return counterMap_.get(shortName).getHourRate();
        }
        if (ending2.equals(".count.3600")) {
          return counterMap_.get(shortName).getHourSum();
        }
      }

      if (fullName.endsWith(".sum")) {
        return counterMap_.get(shortName).getAllTimeSum();
      }
      if (fullName.endsWith(".avg")) {
        return counterMap_.get(shortName).getAllTimeAvg();
      }
      if (fullName.endsWith(".rate")) {
        return counterMap_.get(shortName).getAllTimeRate();
      }
      if (fullName.endsWith(".count")) {
        return counterMap_.get(shortName).getAllTimeSum();
      }
    } catch(Exception e) {
      throw new IllegalArgumentException("Stat name '" + shortName +
        "' not found for '" + ending2 + "' or '" + ending + "'");
    }

    throw new IllegalArgumentException("Stat name argument '" +
                                       fullName + "' not found");
  }

  /**
   * fb303: returns a new map of results and any missing keys will
   * be missing from output map.
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
   * @param fullName  key with the .sum.60 parts, etc
   */
  public boolean hasCounter(String fullName) {
    try {
      getCounter(fullName);
      return true;
    } catch(IllegalArgumentException e) {
      return false;
    }
  }

  /**
   * Extension
   * @param shortName  The stat name used with addStatExportType
   *                   does NOT have the .sum.60 parts, etc
   */
  public boolean containsKey(String shortName) {
    return counterMap_.containsKey(shortName);
  }

  /**
   * fb303
   */
  public Map<String, Long> getCounters()
  {
    Map<String, Long> result = new HashMap<String, Long>();
    String fullname;
    long value;
    Set<String> typeKeys = typeMap_.keySet();
    for (String name : typeKeys) {
      int bitmask = typeMap_.get(name);
      MultiWindowGauge stat = counterMap_.get(name);
      for (ExportType type : ExportType.values()) {
        if ( (bitmask & type.value()) == 0) {
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
