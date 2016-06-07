/*
 * Copyright (C) 2016 Facebook, Inc.
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
package com.facebook.config;

import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestCompositeJSONProvider {

  private CompositeJSONProvider compositeJSONProvider2;
  private CompositeJSONProvider compositeJSONProvider1;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    JSONProvider jsonProvider1 = () -> {
      JSONObject jsonObject = new JSONObject()
        .put("key1", "value1")
        .put("key2", new JSONObject().put("key3", "value3"))
        .put("key4", new JSONObject()
          .put("key5", "value5")
          .put("key7", "value7a")
        );

      return jsonObject;
    };

    JSONProvider jsonProvider2 = () -> {
      JSONObject jsonObject = new JSONObject()
        .put("key3", "value3")
        .put("key2", "value2")
        .put("key4", new JSONObject()
          .put("key6", "value6")
          .put("key7", "value7b")
        );

      return jsonObject;
    };
    compositeJSONProvider1 = new CompositeJSONProvider(jsonProvider1, jsonProvider2);
    compositeJSONProvider2 = new CompositeJSONProvider(jsonProvider2, jsonProvider1);
  }

  @Test
  public void testOrder1() throws Exception {
    JSONObject jsonObject = compositeJSONProvider1.get();

    Assert.assertEquals(jsonObject.get("key1"), "value1");
    Assert.assertEquals(jsonObject.get("key2"), "value2");
    Assert.assertEquals(jsonObject.get("key3"), "value3");
    Assert.assertEquals(jsonObject.getJSONObject("key4").get("key5"), "value5");
    Assert.assertEquals(jsonObject.getJSONObject("key4").get("key6"), "value6");
    Assert.assertEquals(jsonObject.getJSONObject("key4").get("key7"), "value7b");

  }

  @Test
  public void testOrder2() throws Exception {
    JSONObject jsonObject = compositeJSONProvider2.get();

    Assert.assertEquals(jsonObject.get("key1"), "value1");
    Assert.assertEquals(jsonObject.getJSONObject("key2").get("key3"), "value3");
    Assert.assertEquals(jsonObject.get("key3"), "value3");
    Assert.assertEquals(jsonObject.getJSONObject("key4").get("key5"), "value5");
    Assert.assertEquals(jsonObject.getJSONObject("key4").get("key6"), "value6");
    Assert.assertEquals(jsonObject.getJSONObject("key4").get("key7"), "value7a");
  }
}
