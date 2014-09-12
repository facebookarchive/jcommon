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
package com.facebook.collections;

import java.util.Map;
import java.util.Set;

/**
 * Map<K, Set<V>> that is thread-safe. Adding an element V to the
 * set for K will create a set if it doesn't exist already. Conversely, if
 * a set for K becomes empty when removing an item V, the entry for K
 * is removed
 * 
 * @param <K> type for the key
 * @param <V> type of the elements in the set
 */

public interface SetMap<K, V, S extends Set<V>>
  extends Iterable<Map.Entry<K, S>> {

  /**
   * add item to the set for key. Creates the set if not present; removes
   * the set for key if it becomes empty
   *
   * @param key
   * @param item
   */
  public boolean add(K key, V item);

  /**
   * try to remove the set for key.  will return the set if it was removed,
   * or null otherwise.
   *
   * @param key
   * @return
   */
  public S removeSet(K key);

  /**
   * @param key
   * @param item
   * @return true if item was removed from the set for key
   */
  public boolean remove(K key, V item);

  /**
   * get the set associated with key; null if there is no set
   * <p/>
   * NOTE: modifications to this set are reflected within the set
   *
   * @param key the key to pull up a set for
   * @return null if key not present, set for key otherwise
   */
  public S get(K key);

  public void clear();
}
