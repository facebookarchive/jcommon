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

import com.google.common.reflect.TypeToken;

import java.util.HashMap;
import java.util.Map;

public class MixedTypeMap<K> implements ReadOnlyMixedTypeMap<K> {
  private final Map<Key<K, ?>, Object> map;

  public MixedTypeMap(Map<Key<K, ?>, Object> map) {
    this.map = map;
  }

  public MixedTypeMap() {
    this(new HashMap<Key<K, ?>, Object>());
  }

  public <V1, V2 extends V1> void put(K id, Class<V1> clazz, V2 instance) {
    Key<K, V1> key = Key.get(id, clazz);

    map.put(key, instance);
  }

  public <V1, V2 extends V1> void put(K id, TypeToken<V1> clazz, V2 instance) {
    Key<K, V1> key = Key.get(id, clazz);

    map.put(key, instance);
  }

  public <V1, V2 extends V1> void put(Key<K, V1> key, V2 instance) {
    map.put(key, instance);
  }

  public <V> V get(K id, Class<V> clazz) {
    Key<K, V> key = Key.get(id, clazz);

    //noinspection unchecked
    return (V) map.get(key);
  }

  public <V> V get(K id, TypeToken<V> type) {
    Key<K, V> key = Key.get(id, type);

    //noinspection unchecked
    return (V) map.get(key);
  }

  public <V> V get(Key<K, V> key) {
    //noinspection unchecked
    return (V) map.get(key);
  }

  public MixedTypeMap<K> putAll(MixedTypeMap<K> otherMap) {
    map.putAll(otherMap.map);

    return otherMap;
  }

  public int size() {
    return map.size();
  }

  public void clear() {
    map.clear();
  }
}
