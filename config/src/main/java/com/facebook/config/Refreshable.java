package com.facebook.config;

/**
 * object that supports the idea of being 'refreshed'.  
 * Example: a config file accessor that parses the file once and stores
 * it for access.  It may implement this interface so that it may be 
 * reloaded on-demand
 */
public interface Refreshable {
  public void refresh() throws ConfigException;
}
