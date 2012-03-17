package com.facebook.config;

import org.json.JSONException;
import org.json.JSONObject;

// how to extract a particular type from a JSONObject
public interface Extractor<V> {
  public V extract(String key, JSONObject jsonObject) throws JSONException;
}
