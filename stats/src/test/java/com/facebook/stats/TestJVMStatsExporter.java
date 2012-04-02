package com.facebook.stats;

import com.facebook.logging.Logger;
import com.facebook.logging.LoggerImpl;
import com.facebook.stats.mx.JVMStatsExporter;
import com.facebook.stats.mx.Stats;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Test {@link JVMStatsExporter}
 */
public class TestJVMStatsExporter {
  private static final Logger LOG = LoggerImpl.getLogger(TestJVMStatsExporter.class);
  
  @Test(groups = "fast")
  public void testAllStats() throws Exception {
    Stats stats = new Stats();
    new JVMStatsExporter(stats);
    Map<String, Long> exportedStats = getExportedStats(stats);
    Assert.assertTrue(exportedStats.size() > 10);
    // Sort the stats for printing.
    exportedStats = new TreeMap<String, Long>(exportedStats);
    for (Map.Entry<String, Long> entry : exportedStats.entrySet()) {
      // Print the stats for visual examination
      LOG.info("%s = %s", entry.getKey(), entry.getValue());      
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
    new JVMStatsExporter(stats,
                         ".*(UsageThresholdCount|PeakUsage.committed)",
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
