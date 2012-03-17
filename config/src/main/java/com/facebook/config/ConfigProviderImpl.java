package com.facebook.config;

import org.json.JSONException;

import java.io.File;

/**
 * Provides a ConfigAccessor from a JSON data source
 */
public class ConfigProviderImpl implements RefreshableConfigProvider {
  private final JSONProvider jsonProvider;
  private final Object lock = new Object();
  private volatile ConfigAccessor config;

  public ConfigProviderImpl(JSONProvider jsonProvider) {
    this.jsonProvider = jsonProvider;
  }

  public ConfigProviderImpl(File file) {
    this(
      new SystemPropOverridingJSONProvider(
        new ExpandedConfFileJSONProvider(file)
      )
    );
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
