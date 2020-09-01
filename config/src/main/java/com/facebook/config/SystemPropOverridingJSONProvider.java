/*
 * Copyright (C) 2012 Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.config;

import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

/** Load system command-line properties to override JSON key-value pairs */
public class SystemPropOverridingJSONProvider implements JSONProvider {
  private final JSONProvider jsonProvider;

  public SystemPropOverridingJSONProvider(JSONProvider jsonProvider) {
    this.jsonProvider = jsonProvider;
  }

  private JSONObject includeSystemProperties(JSONObject jsonObject) throws JSONException {

    for (Map.Entry<Object, Object> entry : System.getProperties().entrySet()) {
      if (entry.getKey() instanceof String && entry.getValue() instanceof String) {
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
