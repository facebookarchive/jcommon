package com.facebook.config;

import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class TestExpandedConfFileJSONProvider {
  private Map<File, JSONObject> mockFileParser =
    new HashMap<File, JSONObject>();

  public final static String simpleAConfig = "simpleA.json";
  public final static String simpleBConfig = "simpleB.json";
  public final static String simpleIncludeConfig = "simpleInclude.json";
  public final static String multiIncludeConfig = "multiInclude.json";
  public final static String chainedConfig = "chainInclude.json";
  public final static String circularAConfig = "circularIncludeA.json";
  public final static String circularBConfig = "circularIncludeB.json";

  private JSONObject addInclude(JSONObject json, String include)
    throws JSONException {
    return json.append("includes", include);
  }

  private JSONObject addConf(JSONObject json, String key, String value)
    throws JSONException {
    if (!json.has("conf")) {
      json.put("conf", new JSONObject());
    }
    JSONObject confs = json.getJSONObject("conf");
    confs.put(key, value);
    return json;
  }

  private JSONObject buildSimpleAConfig() throws JSONException {
    JSONObject json = new JSONObject();
    json = addConf(json, "key1", "simpleA1");
    json = addConf(json, "key2", "simpleA2");
    return json;
  }

  private JSONObject buildSimpleBConfig() throws JSONException {
    JSONObject json = new JSONObject();
    json = addConf(json, "key1", "simpleB1");
    json = addConf(json, "key3", "simpleB3");
    return json;
  }

  private JSONObject buildSimpleIncludeConfig() throws JSONException {
    JSONObject json = new JSONObject();
    json = addInclude(json, simpleAConfig);
    json = addConf(json, "key1", "simpleInclude1");
    json = addConf(json, "key4", "simpleInclude4");
    return json;
  }

  private JSONObject buildMultiIncludeConfig() throws JSONException {
    JSONObject json = new JSONObject();
    json = addInclude(json, simpleAConfig);
    json = addInclude(json, simpleBConfig);
    json = addConf(json, "key1", "multiInclude1");
    json = addConf(json, "key4", "multiInclude4");
    return json;
  }

  private JSONObject buildChainedConfig() throws JSONException {
    JSONObject json = new JSONObject();
    json = addInclude(json, simpleIncludeConfig);
    json = addConf(json, "key1", "chainInclude1");
    json = addConf(json, "key5", "chainInclude5");
    return json;
  }

  private JSONObject buildCircularIncludeAConfig() throws JSONException {
    JSONObject json = new JSONObject();
    json = addInclude(json, circularBConfig);
    json = addConf(json, "key1", "circularA1");
    json = addConf(json, "key2", "circularA2");
    return json;
  }

  private JSONObject buildCircularIncludeBConfig() throws JSONException {
    JSONObject json = new JSONObject();
    json = addInclude(json, circularAConfig);
    json = addConf(json, "key1", "circularB1");
    json = addConf(json, "key3", "circularB3");
    return json;
  }

  private String getStagingDirPath() {
    // Make it relative to the current user directory
    return new File("").getAbsolutePath() + "/tmp";
  }


  private String buildFullPath(String fileName) {
    return getStagingDirPath() + "/" + fileName;
  }

  private void registerMockFile(String path, JSONObject json) {
    mockFileParser.put(new File(path), json);
  }

  @BeforeTest(alwaysRun = true)
  public void setUp() throws Exception {
    registerMockFile(
      buildFullPath(simpleAConfig), buildSimpleAConfig());
    registerMockFile(
      buildFullPath(simpleBConfig), buildSimpleBConfig());
    registerMockFile(
      buildFullPath(simpleIncludeConfig),buildSimpleIncludeConfig());
    registerMockFile(
      buildFullPath(multiIncludeConfig), buildMultiIncludeConfig());
    registerMockFile(
      buildFullPath(chainedConfig), buildChainedConfig());
    registerMockFile(
      buildFullPath(circularAConfig), buildCircularIncludeAConfig());
    registerMockFile(
      buildFullPath(circularBConfig), buildCircularIncludeBConfig());
  }

  // Return an anonymous inner class that simulates file reading
  private ExpandedConfFileJSONProvider buildJSONProvider(String configPath) {
    return new ExpandedConfFileJSONProvider(new File(configPath)) {
      // Override file parsing so we can get contents w/o having actual files
      protected JSONObject fileToJSON(File file) {
        return mockFileParser.get(file);
      }
    };
  }

  @Test(groups = "fast")
  public void testSimpleConfig() throws Exception {
    JSONObject json =
      buildJSONProvider(buildFullPath(simpleAConfig)).get();

    Assert.assertTrue(json.has("key1"));
    Assert.assertEquals(json.getString("key1"), "simpleA1");
    Assert.assertTrue(json.has("key2"));
    Assert.assertEquals(json.getString("key2"), "simpleA2");
  }

  @Test(groups = "fast")
  public void testSimpleIncludeConfig() throws Exception {
    JSONObject json =
      buildJSONProvider(buildFullPath(simpleIncludeConfig)).get();

    Assert.assertTrue(json.has("key1"));
    Assert.assertEquals(json.getString("key1"), "simpleInclude1");
    Assert.assertTrue(json.has("key2"));
    Assert.assertEquals(json.getString("key2"), "simpleA2");
    Assert.assertTrue(json.has("key4"));
    Assert.assertEquals(json.getString("key4"), "simpleInclude4");
  }

  @Test(groups = "fast")
  public void testMultiIncludeConfig() throws Exception {
    JSONObject json =
      buildJSONProvider(buildFullPath(multiIncludeConfig)).get();

    Assert.assertTrue(json.has("key1"));
    Assert.assertEquals(json.getString("key1"), "multiInclude1");
    Assert.assertTrue(json.has("key2"));
    Assert.assertEquals(json.getString("key2"), "simpleA2");
    Assert.assertTrue(json.has("key3"));
    Assert.assertEquals(json.getString("key3"), "simpleB3");
    Assert.assertTrue(json.has("key4"));
    Assert.assertEquals(json.getString("key4"), "multiInclude4");
  }

  @Test(groups = "fast")
  public void testChainedConfig() throws Exception {
    JSONObject json =
      buildJSONProvider(buildFullPath(chainedConfig)).get();

    Assert.assertTrue(json.has("key1"));
    Assert.assertEquals(json.getString("key1"), "chainInclude1");
    Assert.assertTrue(json.has("key2"));
    Assert.assertEquals(json.getString("key2"), "simpleA2");
    Assert.assertTrue(json.has("key4"));
    Assert.assertEquals(json.getString("key4"), "simpleInclude4");
    Assert.assertTrue(json.has("key5"));
    Assert.assertEquals(json.getString("key5"), "chainInclude5");
  }

  @Test(groups = "fast")
  public void testCircularIncludeConfig() throws Exception {
    JSONObject json =
      buildJSONProvider(buildFullPath(circularAConfig)).get();

    Assert.assertTrue(json.has("key1"));
    Assert.assertEquals(json.getString("key1"), "circularA1");
    Assert.assertTrue(json.has("key2"));
    Assert.assertEquals(json.getString("key2"), "circularA2");
    Assert.assertTrue(json.has("key3"));
    Assert.assertEquals(json.getString("key3"), "circularB3");
    // Running also this proves that there is no infinite loop
  }

}
