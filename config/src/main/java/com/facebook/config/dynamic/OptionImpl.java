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

import com.facebook.logging.Logger;
import com.facebook.logging.LoggerImpl;
import com.google.common.collect.Lists;

import java.util.List;

public class OptionImpl<V> implements Option<V> {
  private static final Logger LOG = LoggerImpl.getClassLogger();
  private final List<OptionWatcher<V>> watchers = Lists.newCopyOnWriteArrayList();

  private volatile V value;

  public OptionImpl() {
  }

  public OptionImpl(V value) {
    this.value = value;
  }

  @Override
  public V getValue() {
    return value;
  }

  @Override
  public synchronized void setValue(V value) {
    this.value = value;

    for (OptionWatcher<V> watcher : watchers) {
      notifyWatcher(watcher, value);
    }
  }

  @Override
  public void addWatcher(OptionWatcher<V> watcher) {
    if (!watchers.contains(watcher)) {
      watchers.add(watcher);
    }
  }

  @Override
  public void removeWatcher(OptionWatcher<V> watcher) {
    watchers.remove(watcher);
  }

  private void notifyWatcher(OptionWatcher<V> watcher, V value) {
    try {
      watcher.propertyUpdated(value);
    } catch (Exception e) {
      LOG.warn(e, "Problem running property watcher for value update: %s", value);
    }
  }
}
