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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Test {@link JVMStatsExporter}
 */
public class TestJVMStatsExporter {
  private static final Logger LOG = LoggerFactory.getLogger(TestJVMStatsExporter.class);
  
  @Test(groups = "fast")
  public void testAllStats() throws Exception {
    Stats stats = new Stats();
    JVMStatsExporter exporter = new JVMStatsExporter(stats);
    Map<String, Long> exportedStats = getExportedStats(stats);
    Assert.assertTrue(exportedStats.size() > 10);
    // Sort the stats for printing.
    exportedStats = new TreeMap<String, Long>(exportedStats);
    for (Map.Entry<String, Long> entry : exportedStats.entrySet()) {
      // Print the stats for visual examination
      LOG.info("{} = {}", entry.getKey(), entry.getValue());
    }
    // Verify that top level numeric stat is available
    Assert.assertTrue(exportedStats.containsKey("jvm.Memory.ObjectPendingFinalizationCount"));
    // Verify that a numeric attribute of composite data is available
    Assert.assertTrue(exportedStats.containsKey("jvm.Memory.HeapMemoryUsage.committed"));
  }
  
  @Test(groups = "fast")
  public void testFilteredStats() throws Exception {
    Stats stats = new Stats();
    // Chose an MBean that has a good chance of being there across different jvm versions
    JVMStatsExporter jvmStatsExporter = new JVMStatsExporter(
      stats,
      ".*(\\.UsageThresholdCount|PeakUsage.committed)",
      "java.lang:type=MemoryPool,name=Code Cache"
    );
    Map<String, Long> exportedStats = getExportedStats(stats);
    Assert.assertEquals(2, exportedStats.size());
    Assert.assertTrue(exportedStats.containsKey("jvm.MemoryPool.Code_Cache.UsageThresholdCount"));
    Assert.assertTrue(exportedStats.containsKey("jvm.MemoryPool.Code_Cache.PeakUsage.committed"));
  }
  
  private static Map<String, Long> getExportedStats(Stats stats) {
    Map<String, Long> statsMap = new HashMap<String, Long>();
    stats.exportCounters(statsMap);
    return statsMap;
  }
}
