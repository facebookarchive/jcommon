package com.facebook.stats;

import com.facebook.stats.mx.Stats;
import com.google.common.collect.ImmutableMap;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class TestStats {
  private Stats stats;
  private static final String notInsertedKey = "not Inserted";
  private static final String key1 = "key1";
  private static final String key2 = "key2";
  private static final String key3 = "key3";
  Map<String, String> attributeMap = new ImmutableMap.Builder<String, String>()
    .put(key1, key2)
    .put(key2, key3)
    .put(key3, key1)
    .build();
  
  @BeforeMethod(alwaysRun = true)
  void setup() {
    stats = new Stats();
  }

  public void verifyStatAttributes() {
    // Test out getAttribute calls
    Assert.assertEquals(stats.getAttribute(key1), key2);
    Assert.assertEquals(stats.getAttribute(key2), key3);
    Assert.assertEquals(stats.getAttribute(key3), key1);
    Assert.assertNull(stats.getAttribute(notInsertedKey));
    stats.setAttribute(key1, key1);
    Assert.assertEquals(stats.getAttribute(key1), key1);
    stats.setAttribute(key1, key2);

    // Test out that the stats attributemap is as expected
    Map <String, String> statsAttributes = stats.getAttributes();
    Assert.assertEquals(attributeMap, statsAttributes);    
  }

  /**
   * Test the setAttribute(String, String) function
   */
  @Test(groups = "fast")
  public void testAttributeString() {
    for (Map.Entry<String, String> attribute: attributeMap.entrySet()) {
      stats.setAttribute(attribute.getKey(), attribute.getValue());
    }
    verifyStatAttributes();

  }

  /**
   * Test the setAttribute(String, Callable <String> ) function
   */
  @Test(groups = "fast")
  public void testAttributeCallable() {
        
    for (final String key: attributeMap.keySet()) {
      stats.setAttribute(key, 
        new Callable<String>() {
          @Override
          public String call() throws Exception {          
            return TestStats.this.attributeMap.get(key);
          }
        });
    }
    verifyStatAttributes();
  }
  
  @Test(groups = "fast")
  public void testGetEmptyCounter() throws Exception {
  	Assert.assertEquals(stats.getCounter("fuu"), 0);
  }

  @Test(groups = "fast")
  public void testDynamicCounters() throws Exception {
    LongWrapper longValue = new LongWrapper(1);
    final String name = "testCounter";
    Assert.assertTrue(stats.addDynamicCounter(name, longValue));
    final Map<String, Long> exported = new HashMap<String, Long>();
    stats.exportCounters(exported);
    Assert.assertEquals(exported.get(name), Long.valueOf(1));
    
    // Test that the value gets exported
    longValue.setValue(123);
    exported.clear();
    stats.exportCounters(exported);
    Assert.assertEquals(exported.get(name), Long.valueOf(123));
    
    // Test that a duplicate set fails to override the previous value
    LongWrapper duplicateValue = new LongWrapper(24);
    Assert.assertFalse(stats.addDynamicCounter(name, duplicateValue));
    exported.clear();
    stats.exportCounters(exported);
    Assert.assertEquals(exported.get(name), Long.valueOf(123));
    
    // Test unset
    Assert.assertTrue(stats.removeDynamicCounter(name));
    exported.clear();
    stats.exportCounters(exported);
    Assert.assertFalse(exported.containsKey(name));
    
    // Test unset for non-existent key
    Assert.assertFalse(stats.removeDynamicCounter(name));
  }

  /**
   * Helper class for testing dynamic counters.
   */
  private static class LongWrapper implements Callable<Long> {
    private LongWrapper(long value) {
      this.value = value;
    }

    public void setValue(long value) {
      this.value = value;
    }

    private long value;
    @Override
    public Long call() throws Exception {
      return value;
    }
  }
}
