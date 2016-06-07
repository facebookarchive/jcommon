/*
 * Copyright (C) 2016 Facebook, Inc.
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class CompositeJSONProvider implements JSONProvider {
  private final List<JSONProvider> jsonProviderList;

  public CompositeJSONProvider(List<JSONProvider> jsonProviderList) {
    this.jsonProviderList = jsonProviderList;
  }

  public CompositeJSONProvider(JSONProvider... jsonProviders) {
    this(Arrays.asList(jsonProviders));
  }

  @Override
  public JSONObject get() throws JSONException {
    JSONObject mergedJsonObject = new JSONObject();

    for (JSONProvider jsonProvider : jsonProviderList) {
      JSONObject jsonObject = jsonProvider.get();

      mergeInto(mergedJsonObject, jsonObject);
    }

    return mergedJsonObject;
  }

  private JSONObject mergeInto(JSONObject destinationJsonObject, JSONObject sourceJsonObject) throws JSONException {
    Iterator keys = sourceJsonObject.keys();

    while (keys.hasNext()) {
      String sourceKey = (String) keys.next();
      Object sourceValue = sourceJsonObject.get(sourceKey);

      if (sourceValue instanceof JSONObject &&
        destinationJsonObject.has(sourceKey) &&
        destinationJsonObject.get(sourceKey) instanceof JSONObject
        ) {
        destinationJsonObject.put(
          sourceKey,
          mergeInto((JSONObject) destinationJsonObject.get(sourceKey), (JSONObject) sourceValue)
        );
      } else {
        destinationJsonObject.put(sourceKey, sourceValue);
      }
    }

    return destinationJsonObject;
  }
}
