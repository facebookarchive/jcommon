package com.facebook.config;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Load system command-line properties to override JSON key-value pairs
 */
public class SystemPropOverridingJSONProvider implements JSONProvider {
  private final JSONProvider jsonProvider;

  public SystemPropOverridingJSONProvider(JSONProvider jsonProvider) {
    this.jsonProvider = jsonProvider;
  }

  private JSONObject includeSystemProperties(
    JSONObject jsonObject
  ) throws JSONException {

    for (Map.Entry<Object, Object> entry : System.getProperties().entrySet()) {
      if (entry.getKey() instanceof String &&
        entry.getValue() instanceof String
        ) {
        String key = (String) entry.getKey();
        String value = (String) entry.getValue();
        // try first to see if value might be a json Object
        try {
          JSONObject valueAsJSONObject = new JSONObject(value);

          jsonObject.put(key, valueAsJSONObject);
        } catch (JSONException e) {
          // means value is just a plain old string
          jsonObject.put(key, value);
        }
      }
    }
    return jsonObject;
  }

  @Override
  public JSONObject get() throws JSONException {
    return includeSystemProperties(jsonProvider.get());
  }
}
