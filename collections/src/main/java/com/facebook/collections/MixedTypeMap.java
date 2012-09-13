package com.facebook.collections;


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

  public <V> void put(K id, Class<? super V> clazz, V instance) {
    Key<K, ?> key = Key.get(id, clazz);

    map.put(key, instance);
  }

  public <V> void put(Key<K,? super V> key, V instance) {
    map.put(key, instance);
  }

  public <V> V get(K id, Class<? extends V> clazz) {
    Key<K, ?> key = Key.get(id, clazz);

    //noinspection unchecked
    return (V) map.get(key);
  }

  public <V> V get(Key<K, V> key) {
    //noinspection unchecked
    return (V) map.get(key);
  }
}
