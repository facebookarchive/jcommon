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

public interface Option<V> {
  public V getValue();

  public void setValue(V value);

  /**
   * Registers a new watcher for this property. If the watcher is already watching this property,
   * this method does nothing.
   *
   * @param watcher a property watcher
   */
  public void addWatcher(OptionWatcher<V> watcher);

  /**
   * Unregisters a watcher. If the watcher is not already watching this property, this method does
   * nothing.
   *
   * @param watcher a property watcher
   */
  public void removeWatcher(OptionWatcher<V> watcher);
}
