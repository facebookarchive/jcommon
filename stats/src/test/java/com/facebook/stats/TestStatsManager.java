package com.facebook.stats;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class TestStatsManager {

  private static String toString(StatsManager s) {
    Map<String, Long> map = s.getCounters();
    StringBuilder result = new StringBuilder();
    for (String fullname : map.keySet()) {
      result.append(fullname + " = " + map.get(fullname) + "\n");
    }
    return result.toString();
  }

  @Test
  public void TestBasic() throws Exception {
    System.out.println("Basic Test");
    StatsManager stats = new StatsManager();
    long num = 100, amt = 6;

    for (long i=0; i<num; i++) {
      stats.addStatValue("basic", amt);
    }
    System.out.println(toString(stats));

    Assert.assertEquals(amt, stats.getCounter("basic.avg"));
    Assert.assertEquals(amt, stats.getCounter("basic.avg.60"));
    Assert.assertEquals(amt, stats.getCounter("basic.avg.600"));
    Assert.assertEquals(amt, stats.getCounter("basic.avg.3600"));

    stats.addStatExportType("test-sum", HistoryManager.ExportType.SUM);
    for (int i=0; i<num; i++) {
      stats.addStatValue("test-sum", 1);
    }
    System.out.println(toString(stats));

    Assert.assertEquals(num, stats.getCounter("test-sum.sum"));
    Assert.assertEquals(num, stats.getCounter("test-sum.sum.60"));
    Assert.assertEquals(num, stats.getCounter("test-sum.sum.600"));
    Assert.assertEquals(num, stats.getCounter("test-sum.sum.3600"));

    stats.addStatExportType("test-rate", HistoryManager.ExportType.RATE);
    stats.addStatExportType("test-avg", HistoryManager.ExportType.AVG);
    stats.addStatExportType("test-count", HistoryManager.ExportType.COUNT);
    for (int i=0; i<num; i++) {
      stats.addStatValue("test-rate", amt);
      stats.addStatValue("test-avg", amt);
      stats.addStatValue("test-count", amt);
    }
    Assert.assertEquals(amt*num, stats.getCounter("test-count.count"));

    stats.addStatExportType("test-all", HistoryManager.ExportType.SUM);
    stats.addStatExportType("test-all", HistoryManager.ExportType.RATE);
    stats.addStatExportType("test-all", HistoryManager.ExportType.AVG);
    stats.addStatExportType("test-all", HistoryManager.ExportType.COUNT);

    System.out.println(toString(stats));

    stats.addStatValue("test-all", amt);
    stats.addStatValue("test-all", amt);

    Assert.assertEquals(amt+amt, stats.getCounter("test-all.sum"));
    Assert.assertEquals(amt, stats.getCounter("test-all.avg"));
    Assert.assertEquals(amt+amt, stats.getCounter("test-all.count"));
  }
}
