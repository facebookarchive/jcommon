package com.facebook.config;

import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicReference;

public class TestFileJSONProvider {

  private AtomicReference<BufferedReader> readerReference;
  private FileJSONProvider jsonProvider;
  private String jsonWithComments;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    readerReference = new AtomicReference<BufferedReader>();
    jsonProvider = new FileJSONProvider(null) {
      @Override
      protected BufferedReader getReader() throws FileNotFoundException {
        return readerReference.get();
      }
    };
    jsonWithComments = "{\n" +
      "\tkey : \"value\",\n" +
      "\t// comment 1\n" +
      "\tnested : { x : 1 }\n" +
      "\t//nested2 : { y : 2 }\n" +
      "// comment 2\n" +
      "\t// comment 3\n" +
      "}";
  }

  @Test(groups = "fast")
  public void testComment() throws Exception {
    BufferedReader reader = new BufferedReader(
      new InputStreamReader(
        new ByteArrayInputStream(jsonWithComments.getBytes())
      )
    );
    readerReference.set(reader);
    
    JSONObject jsonObject = jsonProvider.get();
    Assert.assertEquals(jsonObject.getString("key"), "value");
    Assert.assertTrue(jsonObject.has("nested"));
  }
}