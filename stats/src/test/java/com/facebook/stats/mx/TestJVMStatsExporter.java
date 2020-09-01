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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Test {@link JVMStatsExporter} */
public class TestJVMStatsExporter {
  private static final Logger LOG = LoggerImpl.getLogger(TestJVMStatsExporter.class);

  @Test
  public void testAllStats() throws Exception {
    Stats stats = new Stats();
    JVMStatsExporter exporter = new JVMStatsExporter(stats);
    Map<String, Long> exportedStats = getExportedStats(stats);
    Assert.assertTrue(exportedStats.size() > 10);
    // Sort the stats for printing.
    exportedStats = new TreeMap<>(exportedStats);
    for (Map.Entry<String, Long> entry : exportedStats.entrySet()) {
      // Print the stats for visual examination
      LOG.info("%s = %s", entry.getKey(), entry.getValue());
    }
    // Verify that top level numeric stat is available
    Assert.assertTrue(exportedStats.containsKey("jvm.Memory.ObjectPendingFinalizationCount"));
    // Verify that a numeric attribute of composite data is available
    Assert.assertTrue(exportedStats.containsKey("jvm.Memory.HeapMemoryUsage.committed"));
  }

  @Test
  public void testFilteredStats() throws Exception {
    Stats stats = new Stats();
    // part of the standard API in JDK8 and later
    JVMStatsExporter jvmStatsExporter =
        new JVMStatsExporter(stats, ".*(HeapMemoryUsage).*", "java.lang:type=Memory");
    Map<String, Long> exportedStats = getExportedStats(stats);
    Assert.assertTrue(exportedStats.size() >= 2, "expected both stats in " + exportedStats);
    Assert.assertTrue(
        exportedStats.containsKey("jvm.Memory.HeapMemoryUsage.max"),
        "expected HeapMemoryUsage.max in " + exportedStats);
    Assert.assertTrue(
        exportedStats.containsKey("jvm.Memory.HeapMemoryUsage.used"),
        "expected HeapMemoryUsage.used in " + exportedStats);
  }

  @Test
  public void testStatNameReplacer() throws Exception {
    Stats stats = new Stats();
    // part of the standard API in JDK8 and later
    JVMStatsExporter jvmStatsExporter =
        new JVMStatsExporter(
            stats,
            (bean, attribute, key) -> {
              String name = "test";

              if (attribute != null) {
                name += "." + attribute;
              }

              if (key != null) {
                name += "." + key;
              }

              return Optional.of(name);
            },
            "java.lang:type=Memory");
    Map<String, Long> exportedStats = getExportedStats(stats);
    Assert.assertTrue(exportedStats.containsKey("test.HeapMemoryUsage.used"));
    Assert.assertTrue(exportedStats.containsKey("test.HeapMemoryUsage.max"));
  }

  @Test
  public void testStatNameReplacerFilterAll() throws Exception {
    Stats stats = new Stats();
    // Chose an MBean that has a good chance of being there across different jvm versions
    JVMStatsExporter jvmStatsExporter =
        new JVMStatsExporter(
            stats,
            (bean, attribute, key) -> Optional.empty(),
            "java.lang:type=MemoryPool,name=Code Cache");
    Map<String, Long> exportedStats = getExportedStats(stats);
    Assert.assertEquals(exportedStats.size(), 0);
  }

  private static Map<String, Long> getExportedStats(Stats stats) {
    Map<String, Long> statsMap = new HashMap<>();
    stats.exportCounters(statsMap);
    return statsMap;
  }
}
