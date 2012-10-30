package com.facebook.config.dynamic;

public interface Option<V> {
  public V getValue();

  public void setValue(V value);

  /**
   * Registers a new watcher for this property.  If the watcher is already watching this property,
   * this method does nothing.
   *
   * @param watcher a property watcher
   */
  public void addWatcher(OptionWatcher<V> watcher);

  /**
   * Unregisters a watcher.  If the watcher is not already watching this property, this method does
   * nothing.
   *
   * @param watcher a property watcher
   */
  public void removeWatcher(OptionWatcher<V> watcher);
}
