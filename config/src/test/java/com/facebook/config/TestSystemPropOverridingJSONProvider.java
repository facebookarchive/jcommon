package com.facebook.config;

import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestSystemPropOverridingJSONProvider {
  private static final String KEY1 = "key1";
  private static final String KEY2 = "key2";
  private static final String KEY3 = "key3";
  private static final String KEY4 = "key4";
  private static final String KEY5 = "key5";

  private JSONProvider jsonProvider;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put(KEY1, "value1");
    jsonObject.put(KEY2, "value2");
    jsonProvider =
      new SystemPropOverridingJSONProvider(new MockJSONProvider(jsonObject));
  }

  @AfterMethod(alwaysRun = true)
  public void tearDown() throws Exception {
    System.clearProperty(KEY1);
    System.clearProperty(KEY2);
    System.clearProperty(KEY3);
    System.clearProperty(KEY4);
  }

  @Test(groups = "fast")
  public void testNewProps() throws Exception {
    System.setProperty(KEY3, "propvalue1");
    System.setProperty(KEY4, "propvalue2");
    JSONObject jsonObject = jsonProvider.get();
    Assert.assertEquals(jsonObject.getString(KEY1), "value1");
    Assert.assertEquals(jsonObject.getString(KEY2), "value2");
    Assert.assertEquals(jsonObject.getString(KEY3), "propvalue1");
    Assert.assertEquals(jsonObject.getString(KEY4), "propvalue2");
  }

  @Test(groups = "fast")
  public void testOverride() throws Exception {
    System.setProperty(KEY3, "propvalue1");
    System.setProperty(KEY2, "propvalue2");
    JSONObject jsonObject = jsonProvider.get();
    Assert.assertEquals(jsonObject.getString(KEY1), "value1");
    Assert.assertEquals(jsonObject.getString(KEY2), "propvalue2");
    Assert.assertEquals(jsonObject.getString(KEY3), "propvalue1");
  }
  
  @Test(groups = "fast")
  public void testValueAsJSON() throws Exception {
  	System.setProperty(KEY5, "{nested_key : nested_value}");
    JSONObject jsonObject = jsonProvider.get();
    JSONObject nestedJSONObject = jsonObject.getJSONObject(KEY5);

    Assert.assertEquals(nestedJSONObject.get("nested_key"), "nested_value");
  }
}
