package com.facebook.stats;

/*
 * This is the interface that mirrors
 * fbcode/common/fb303/cpp/FacebookBase.h functionality for adding
 * stats that track their history over 60 secs, 600 secs, 3600 secs,
 * and "alltime".  Any class that implements HistoryMgrIf will
 * implement FacebookServiceIf (see thrift files) as well.
 *
 * HistoryMgrIf manages a set of stats.  Stats are usually registered
 * with a call to addStatExportType(name,etype) and then set or
 * incremented many times with addStatValue(name,delta).  The export
 * type supports reporting different summaries of the stat (sum, avg,
 * rate, count).
 *
 * For example, an event counter can be exported with "sum" and "avg"
 * and if it's incremented once a second with addStatValue("c", 10)
 * the getCounters() call will report "c.sum.60 == 600", "c.avg.60 ==
 * 10", "c.sum.600 = 6000", etc.  (Assuming the underlying
 * implementation uses a MultiWindowRate counter which reports stats
 * histories for 60, 600, 3600, and "alltime".)
 *
 */
public interface HistoryManager {

  public enum ExportType {
    NONE(0), SUM(1), COUNT(2), AVG(4), RATE(8);
    private int value;
    ExportType(int v) { this.value = v; }
    public String toString() { return super.toString().toLowerCase(); }
    public int value() { return value; }
  };

  public void addStatExportType(String name, ExportType etype);
  public void addStatValue(String name, long delta);

  /**
   * Typically you will implement this from FacebookServiceIf
   * to actually read the counters/stats created
   *
   * public Map<String, Long> getCounters();
   */

}

