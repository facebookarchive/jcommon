package com.facebook.config;

import org.json.JSONException;
import org.json.JSONObject;

public class BooleanExtractor implements Extractor<Boolean> {
  @Override
  public Boolean extract(String key, JSONObject jsonObject)
    throws JSONException {
    return jsonObject.getBoolean(key);
  }
}
