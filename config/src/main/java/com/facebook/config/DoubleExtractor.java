package com.facebook.config;

import org.json.JSONException;
import org.json.JSONObject;

public class DoubleExtractor implements Extractor<Double> {
  @Override
  public Double extract(String key, JSONObject jsonObject)
    throws JSONException {
    return jsonObject.getDouble(key);
  }
}
