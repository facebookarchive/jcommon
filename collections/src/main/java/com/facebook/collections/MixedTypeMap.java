package com.facebook.collections;


import com.google.common.reflect.TypeToken;

import java.util.HashMap;
import java.util.Map;

public class MixedTypeMap<K> {
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
}
