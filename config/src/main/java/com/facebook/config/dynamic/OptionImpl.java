package com.facebook.config.dynamic;

import com.facebook.logging.Logger;
import com.facebook.logging.LoggerImpl;
import com.google.common.collect.Lists;

import java.util.List;

public class OptionImpl<V> implements Option<V> {
  private static final Logger LOG = LoggerImpl.getLogger(OptionImpl.class);

  private final List<OptionWatcher<V>> watchers = Lists.newCopyOnWriteArrayList();

  private volatile V value;

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
