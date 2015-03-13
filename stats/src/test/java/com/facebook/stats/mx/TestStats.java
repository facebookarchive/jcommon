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

import com.google.common.collect.ImmutableMap;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class TestStats {
  private static final String NOT_INSERTED_KEY = "not Inserted";
  private static final String KEY1 = "key1";
  private static final String KEY2 = "key2";
  private static final String KEY3 = "key3";
  private static final String ZERO_COUNTER = "to-0-counter";
  private static final String COUNTER_TO_SET = "count-to-set";
  private static final String COUNTER_NOT_SET = "counter-not-set";

  private Map<String, String> attributeMap = new ImmutableMap.Builder<String, String>()
    .put(KEY1, KEY2)
    .put(KEY2, KEY3)
    .put(KEY3, KEY1)
    .build();

  private Stats stats;

  @BeforeMethod(alwaysRun = true)
  void setup() {
    stats = new Stats();
  }

  public void verifyStatAttributes() {
    // Test out getAttribute calls
    Assert.assertEquals(stats.getAttribute(KEY1), KEY2);
    Assert.assertEquals(stats.getAttribute(KEY2), KEY3);
    Assert.assertEquals(stats.getAttribute(KEY3), KEY1);
    Assert.assertNull(stats.getAttribute(NOT_INSERTED_KEY));
    stats.setAttribute(KEY1, KEY1);
    Assert.assertEquals(stats.getAttribute(KEY1), KEY1);
    stats.setAttribute(KEY1, KEY2);

    // Test out that the stats attributemap is as expected
    Map<String, String> statsAttributes = stats.getAttributes();
    Assert.assertEquals(attributeMap, statsAttributes);
  }

  /**
   * Test the setAttribute(String, String) function
   */
  @Test(groups = "fast")
  public void testAttributeString() {
    for (Map.Entry<String, String> attribute : attributeMap.entrySet()) {
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
  public void testRemoveCounter() throws Exception {
    stats.incrementCounter("fuu", 1);
    Assert.assertTrue(stats.removeCounter("fuu"));
    Assert.assertFalse(stats.removeCounter("fuu"));
    Map<String, Long> counterMap = new HashMap<>();
    stats.exportCounters(counterMap);
    Assert.assertNull(counterMap.get("fuu"));
  }

  @Test(groups = "fast")
  public void testRemoveAttribute() throws Exception {
    stats.setAttribute("bar", "bar");
    Assert.assertTrue(stats.removeAttribute("bar"));
    Assert.assertFalse(stats.removeAttribute("bar"));
    Assert.assertNull(stats.getAttributes().get("bar"));
  }

  @Test(groups = "fast")
  public void testDynamicCounters() throws Exception {
    LongWrapper longValue = new LongWrapper(1);
    final String name = "testCounter";
    Assert.assertTrue(stats.addDynamicCounter(name, longValue));
    final Map<String, Long> exported = new HashMap<>();
    stats.exportCounters(exported);
    Assert.assertEquals(exported.get(name), Long.valueOf(1));

    // Test that the value gets exported
    longValue.setValue(123);
    exported.clear();
    stats.exportCounters(exported);
    Assert.assertEquals(exported.get(name), Long.valueOf(123));
    Assert.assertEquals(stats.getCounter(name), 123L);

    // Test that a duplicate set fails to override the previous value
    LongWrapper duplicateValue = new LongWrapper(24);
    Assert.assertFalse(stats.addDynamicCounter(name, duplicateValue));
    exported.clear();
    stats.exportCounters(exported);
    Assert.assertEquals(exported.get(name), Long.valueOf(123));
    Assert.assertEquals(stats.getCounter(name), 123L);

    // Test unset
    Assert.assertTrue(stats.removeCounter(name));
    exported.clear();
    stats.exportCounters(exported);
    Assert.assertFalse(exported.containsKey(name));
    Assert.assertEquals(stats.getCounter(name), 0L);

    // Test unset for non-existent key
    Assert.assertFalse(stats.removeAttribute(name));
  }

  @Test(groups = "fast")
  public void testGetDynamicCounters() throws Exception {
    LongWrapper longValue = new LongWrapper(1);
    final String name = "testCounter";
    Assert.assertTrue(stats.addDynamicCounter(name, longValue));
    Callable<Long> dynamicCounter = stats.getDynamicCounter(name);
    Assert.assertEquals(dynamicCounter.call().longValue(), 1);
  }

  @Test(groups = "fast")
  public void testSetCounterValue() throws Exception {
    stats.incrementCounter(COUNTER_TO_SET, 2);
    stats.incrementCounter(COUNTER_TO_SET, 20);
    stats.incrementCounter(COUNTER_TO_SET, 200);
    Assert.assertEquals(stats.getCounter(COUNTER_TO_SET), 222);
    Assert.assertEquals(StatsUtil.setCounterValue(COUNTER_TO_SET, 1001, stats), 222);
    Assert.assertEquals(stats.getCounter(COUNTER_TO_SET), 1001);
  }

  @Test(groups = "fast")
  public void testReset() throws Exception {
    Assert.assertEquals(stats.getCounter(ZERO_COUNTER), 0);
    stats.incrementCounter(ZERO_COUNTER, 1);
    stats.incrementCounter(ZERO_COUNTER, 10);
    stats.incrementCounter(ZERO_COUNTER, 100);
    Assert.assertEquals(stats.resetCounter(ZERO_COUNTER), 111);
    Assert.assertEquals(stats.getCounter(ZERO_COUNTER), 0);
  }

  @Test(groups = "fast")
  public void testResetBeforeCallingIncrementOrGet() {
    stats.resetCounter(COUNTER_NOT_SET);
    Assert.assertEquals(stats.getCounter(COUNTER_NOT_SET), 0);
  }

  /**
   * Helper class for testing dynamic counters.
   */
  private static class LongWrapper implements Callable<Long> {
    private long value;

    private LongWrapper(long value) {
      this.value = value;
    }

    public void setValue(long value) {
      this.value = value;
    }

    @Override
    public Long call() throws Exception {
      return value;
    }
  }
}
