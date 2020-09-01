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

import java.io.File;
import org.json.JSONException;

/** Provides a ConfigAccessor from a JSON data source */
public class ConfigProviderImpl implements RefreshableConfigProvider {
  private final JSONProvider jsonProvider;
  private final Object lock = new Object();
  private volatile ConfigAccessor config;

  public ConfigProviderImpl(JSONProvider jsonProvider) {
    this.jsonProvider = jsonProvider;
  }

  public ConfigProviderImpl(File file) {
    this(new SystemPropOverridingJSONProvider(new ExpandedConfFileJSONProvider(file)));
  }

  @Override
  public ConfigAccessor getConfig() {
    if (config == null) {
      synchronized (lock) {
        if (config == null) {
          refresh();
        }
      }
    }

    return config;
  }

  @Override
  public void refresh() {
    try {
      config = new ConfigAccessor(jsonProvider.get());
    } catch (JSONException e) {
      throw new ConfigException(e);
    }
  }
}
