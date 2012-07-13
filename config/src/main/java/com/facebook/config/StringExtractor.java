package com.facebook.config;

import org.json.JSONException;
import org.json.JSONObject;

public class StringExtractor implements Extractor<String> {
  @Override
  public String extract(String key, JSONObject jsonObject) throws JSONException {
    Object obj = jsonObject.get(key);

    if (obj instanceof String) {
      return (String) obj;
    } else {
      return String.valueOf(obj);
    }
  }
}
