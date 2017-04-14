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
package com.facebook.config.dynamic;

import com.google.common.collect.Maps;

import java.util.concurrent.ConcurrentMap;

import com.facebook.logging.Logger;
import com.facebook.logging.LoggerImpl;

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
