package com.facebook.collections;


import org.junit.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestMixedTypeMap {

  private MixedTypeMap<String> stringMap;
  private String key1;
  private Pair<Long,Long> value2;
  private Key<String,Pair> key2;
  private Key<String,CounterMap> key3;
  private CounterMap<String> value3;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    stringMap = new MixedTypeMap<String>();
    key1 = "fuu";
    value2 = new Pair<Long, Long>(200L, 800L);
    key2 = Key.get("fuu", Pair.class);
    key3 = Key.get("c-map", CounterMap.class);
    value3 = new CounterMap<String>();
    value3.addAndGet("a", 1);
    value3.addAndGet("b", 2);
    value3.addAndGet("c", 3);

    stringMap.put(key1, Long.class, 100L);
    stringMap.put(key2, value2);
    stringMap.put(key3, value3);
  }

  @Test(groups = "fast")
  public void testSanity1() throws Exception {
    Long value = stringMap.get(key1, Long.class);

    Assert.assertEquals(value.longValue(), 100L);
  }

  @Test(groups = "fast")
  public void testSanity2() throws Exception {
    Pair pair = stringMap.get(key2);

    Assert.assertEquals(pair.getFirst(), 200L);
    Assert.assertEquals(pair.getSecond(), 800L);
  }

  @Test(groups = "fast")
  public void testSanity3() throws Exception {
    CounterMap<String> value  = stringMap.get(key3);

    Assert.assertEquals(value.get("a"), 1);
    Assert.assertEquals(value.get("b"), 2);
    Assert.assertEquals(value.get("c"), 3);
  }
}