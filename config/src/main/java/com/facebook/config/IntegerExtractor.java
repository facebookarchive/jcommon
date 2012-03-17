package com.facebook.config;

import org.json.JSONException;
import org.json.JSONObject;

public class IntegerExtractor implements Extractor<Integer> {
  @Override
  public Integer extract(String key, JSONObject jsonObject)
    throws JSONException {
    return jsonObject.getInt(key);
  }
}
