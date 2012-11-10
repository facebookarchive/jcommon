package com.facebook.collections;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentSetFactory<V> implements SetFactory<V, Set<V>> {
  @Override
  public Set<V> create() {
    return Collections.newSetFromMap(new ConcurrentHashMap<V, Boolean>());
  }
}
