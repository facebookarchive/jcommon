package com.facebook.config.dynamic;

import com.facebook.logging.Logger;
import com.facebook.logging.LoggerImpl;
import com.google.common.collect.Maps;

import java.util.concurrent.ConcurrentMap;

public class StringOptions {
  private static final Logger LOG = LoggerImpl.getLogger(StringOptions.class);

  private final ConcurrentMap<String, Option<String>> optionMap = Maps.newConcurrentMap();

  public Option<String> getOption(String key) {
    Option<String> option = new OptionImpl<String>();
    Option<String> existing = optionMap.putIfAbsent(key, option);

    return existing == null ? option : existing;
  }

  public void setOption(String key, String value) {
    LOG.info("Setting option %s to %s", key, value);

    Option<String> option = getOption(key);

    option.setValue(value);
  }
}
