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

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class TestStatsManager {

  private static String toString(StatsManager s) {
    Map<String, Long> map = s.getCounters();
    StringBuilder result = new StringBuilder();
    for (Map.Entry<String, Long> entry : map.entrySet()) {
      String fullname = entry.getKey();
      result.append(fullname + " = " + map.get(fullname) + "\n");
    }
    return result.toString();
  }

  private void assertCounter(StatsManager stats, String name, long value) {
    Assert.assertEquals(stats.getCounter(name), value);
    Assert.assertNotNull(stats.getCounters().get(name));
    Assert.assertEquals((long)stats.getCounters().get(name), value);
  }

  @Test(groups="fast")
  public void TestBasic() throws Exception {
    System.out.println("Basic Test");
    StatsManager stats = new StatsManager();
    long num = 100, amt = 6;

    for (long i=0; i<num; i++) {
      stats.addStatValue("basic", amt);
    }
    System.out.println(toString(stats));

    assertCounter(stats, "basic.avg", amt);
    assertCounter(stats, "basic.avg.60", amt);
    assertCounter(stats, "basic.avg.600", amt);
    assertCounter(stats, "basic.avg.3600", amt);

    stats.addStatExportType("test-sum", HistoryManager.ExportType.SUM);
    for (int i=0; i<num; i++) {
      stats.addStatValue("test-sum", amt);
    }
    System.out.println(toString(stats));

    assertCounter(stats, "test-sum.sum", num*amt);
    assertCounter(stats, "test-sum.sum.60", num*amt);
    assertCounter(stats, "test-sum.sum.600", num*amt);
    assertCounter(stats, "test-sum.sum.3600", num*amt);

    stats.addStatExportType("test-rate", HistoryManager.ExportType.RATE);
    stats.addStatExportType("test-avg", HistoryManager.ExportType.AVG);
    stats.addStatExportType("test-count", HistoryManager.ExportType.COUNT);
    for (int i=0; i<num; i++) {
      stats.addStatValue("test-rate", amt);
      stats.addStatValue("test-avg", amt);
      stats.addStatValue("test-count", amt);
    }

    System.out.println(toString(stats));
    assertCounter(stats, "test-count.count", num);
    assertCounter(stats, "test-count.count.60", num);
    assertCounter(stats, "test-count.count.600", num);
    assertCounter(stats, "test-count.count.3600", num);

    assertCounter(stats, "test-avg.avg", amt);
    assertCounter(stats, "test-avg.avg.60", amt);
    assertCounter(stats, "test-avg.avg.600", amt);
    assertCounter(stats, "test-avg.avg.3600", amt);

    stats.addStatExportType("test-all", HistoryManager.ExportType.SUM);
    stats.addStatExportType("test-all", HistoryManager.ExportType.RATE);
    stats.addStatExportType("test-all", HistoryManager.ExportType.AVG);
    stats.addStatExportType("test-all", HistoryManager.ExportType.COUNT);


    stats.addStatValue("test-all", amt);
    stats.addStatValue("test-all", amt);

    System.out.println(toString(stats));
    assertCounter(stats, "test-all.sum", amt+amt);
    assertCounter(stats, "test-all.avg", amt);
    assertCounter(stats, "test-all.count", 2);
  }
}
