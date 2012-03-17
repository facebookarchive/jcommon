package com.facebook.collections;

import java.util.Set;

public class ConcurrentSetMap<K, V> extends SetMapImpl<K, V, Set<V>> {
  public ConcurrentSetMap() {
    super(new ConcurrentSetFactory<V>());
  }
}
