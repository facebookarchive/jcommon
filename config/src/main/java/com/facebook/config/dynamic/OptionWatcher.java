package com.facebook.config.dynamic;

public interface OptionWatcher<V> {
  public void propertyUpdated(V value) throws Exception;
}
