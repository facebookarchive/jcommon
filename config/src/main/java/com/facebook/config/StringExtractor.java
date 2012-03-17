package com.facebook.config;

import org.json.JSONException;
import org.json.JSONObject;

public class StringExtractor implements Extractor<String> {
  @Override
  public String extract(String key, JSONObject jsonObject)
    throws JSONException {
    return jsonObject.getString(key);
  }
}
