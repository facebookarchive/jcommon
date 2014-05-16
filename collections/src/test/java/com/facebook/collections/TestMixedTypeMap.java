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
package com.facebook.collections;


import com.facebook.collectionsbase.Piles;
import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

public class TestMixedTypeMap {
  private MixedTypeMap<String> stringMap1;
  private MixedTypeMap<String> stringMap2;
  private String key1;
  private Pair<Long, Long> value2;
  private Key<String, Pair> key2;
  private Key<String, CounterMap> key3;
  private CounterMap<String> value3;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    stringMap1 = new MixedTypeMap<>();
    stringMap2 = new MixedTypeMap<>();
    key1 = "fuu";
    value2 = new Pair<>(200L, 800L);
    key2 = Key.get("fuu", Pair.class);
    key3 = Key.get("c-map", CounterMap.class);
    value3 = new CounterMap<>();
    value3.addAndGet("a", 1);
    value3.addAndGet("b", 2);
    value3.addAndGet("c", 3);

    stringMap1.put(key1, Long.class, 100L);
    stringMap1.put(key2, value2);
    stringMap1.put(key3, value3);
  }

  @Test(groups = "fast")
  public void testSanity1() throws Exception {
    Long value = stringMap1.get(key1, Long.class);

    Assert.assertEquals(value.longValue(), 100L);
  }

  @Test(groups = "fast")
  public void testSanity2() throws Exception {
    Pair pair = stringMap1.get(key2);

    Assert.assertEquals(pair.getFirst(), 200L);
    Assert.assertEquals(pair.getSecond(), 800L);
  }

  @Test(groups = "fast")
  public void testSanity3() throws Exception {
    CounterMap<String> value = stringMap1.get(key3);

    Assert.assertEquals(value.get("a"), 1);
    Assert.assertEquals(value.get("b"), 2);
    Assert.assertEquals(value.get("c"), 3);
  }

  @Test(groups = "fast")
  public void testSuperType() throws Exception {
    Key<String, Object> objectKey = Key.get("x", Object.class);
    Key<String, Number> numberKey = Key.get("x", Number.class);
    Key<String, Float> floatKey = Key.get("x", Float.class);

    asssertSuperTestResults(objectKey, numberKey, floatKey);
  }

  @Test(groups = "fast")
  public void testSuperUsingTypeToken() throws Exception {
    // version that use the type token directly
    Key<String, Object> objectKey = Key.get("x", TypeToken.of(Object.class));
    Key<String, Number> numberKey = Key.get("x", TypeToken.of(Number.class));
    Key<String, Float> floatKey = Key.get("x", TypeToken.of(Float.class));

    asssertSuperTestResults(objectKey, numberKey, floatKey);
  }

  private void asssertSuperTestResults(
    Key<String, Object> objectKey,
    Key<String, Number> numberKey,
    Key<String, Float> floatKey
  ) {
    stringMap1.put(objectKey, 37);
    stringMap1.put(numberKey, 4.16);
    // obvious
    Assert.assertEquals(stringMap1.get(objectKey), 37);
    Assert.assertEquals(stringMap1.get(numberKey), 4.16);
    //should be obvious: readers/writers agree on what key for a given class to use if it implements
    // several interfaces, not just the inheritance case
    Assert.assertNull(stringMap1.get(floatKey));
  }

  @Test(groups = "fast")
  public void testTypeTokenAndClass() throws Exception {
    Key<String, Integer> classKey = Key.get("baz", Integer.class);
    Key<String, Integer> typeKey = Key.get("baz", TypeToken.of(Integer.class));

    stringMap1.put(classKey, Integer.MAX_VALUE);
    Assert.assertEquals(stringMap1.get(typeKey).longValue(), Integer.MAX_VALUE);
    Assert.assertEquals(stringMap1.get(classKey).longValue(), Integer.MAX_VALUE);
    stringMap1.put(typeKey, Integer.MIN_VALUE);
    Assert.assertEquals(stringMap1.get(typeKey).longValue(), Integer.MIN_VALUE);
    Assert.assertEquals(stringMap1.get(classKey).longValue(), Integer.MIN_VALUE);
  }

  @Test(groups = "fast")
  public void testGenerics() throws Exception {
    TypeToken<List<String>> stringTypeToken = new TypeToken<List<String>>() {};
    List<String> stringList = Piles.<String>copyOf(
      ImmutableList.<String>builder()
        .add("x")
        .add("y")
        .add("z")
        .add("1")
        .build().iterator()
    );

    stringMap1.put("fuu", stringTypeToken, stringList);
    Key myKey = Key.get("fuu", stringTypeToken);

    Assert.assertEquals(stringMap1.get(myKey), stringList);
    Assert.assertEquals(stringMap1.get("fuu", stringTypeToken), stringList);
  }

  @Test(groups = "fast")
  public void testPutAll() throws Exception {
    stringMap1.put("key1", String.class, "value1");
    stringMap1.put("key2", String.class, "value2");
    stringMap1.put("key3", String.class, "value3");
    stringMap2.put("key4", String.class, "value4");
    stringMap2.putAll(stringMap1);

    Assert.assertEquals(stringMap2.get("key1", String.class), "value1");
    Assert.assertEquals(stringMap2.get("key2", String.class), "value2");
    Assert.assertEquals(stringMap2.get("key3", String.class), "value3");
    Assert.assertEquals(stringMap2.get("key4", String.class), "value4");
    Assert.assertEquals(stringMap2.size(), stringMap1.size() + 1, "stringMap2 size");
    Assert.assertEquals(stringMap1.size(), 6, "stringMap1 size");
  }
}
