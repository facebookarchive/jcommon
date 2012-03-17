package com.facebook.config;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Interface for providing JSON Objects
 */
public interface JSONProvider {
  public JSONObject get() throws JSONException;
}