package com.facebook.config;

import org.json.JSONException;
import org.json.JSONObject;

public class ClassExtractor implements Extractor<Class<?>> {
  @Override
  public Class<?> extract(String key, JSONObject jsonObject)
    throws JSONException {
    try {
      return Class.forName(jsonObject.getString(key));
    } catch (ClassNotFoundException e) {
      throw new JSONException(e);
    }
  }
}
