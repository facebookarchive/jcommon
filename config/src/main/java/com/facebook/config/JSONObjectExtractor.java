package com.facebook.config;

import org.json.JSONException;
import org.json.JSONObject;

public class JSONObjectExtractor implements Extractor<JSONObject> {
  @Override
  public JSONObject extract(String key, JSONObject jsonObject)
    throws JSONException {
    return jsonObject.getJSONObject(key);
  }
}
