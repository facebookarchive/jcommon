package com.facebook.config;

import org.json.JSONException;
import org.json.JSONObject;

public class MockJSONProvider implements JSONProvider {
  private final JSONObject jsonObject;

  public MockJSONProvider(JSONObject jsonObject) {
    this.jsonObject = jsonObject;
  }

  @Override
  public JSONObject get() throws JSONException {
    return jsonObject;
  }
}
