package com.facebook.collections;

import java.util.Set;

public interface SetFactory<V, S extends Set<V>> {
  public S create();
}
