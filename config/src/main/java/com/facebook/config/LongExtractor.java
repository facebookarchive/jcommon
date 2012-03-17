package com.facebook.config;

import org.json.JSONException;
import org.json.JSONObject;

public class LongExtractor implements Extractor<Long> {
  @Override
  public Long extract(String key, JSONObject jsonObject)
    throws JSONException {
    return jsonObject.getLong(key);
  }
}
