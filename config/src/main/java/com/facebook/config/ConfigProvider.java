package com.facebook.config;

public interface ConfigProvider {
  /**
   * Provides an accessor for a config source
   * 
   * @return - ConfigAccessor
   * @throws ConfigException
   */
  public ConfigAccessor getConfig() throws ConfigException;
}
