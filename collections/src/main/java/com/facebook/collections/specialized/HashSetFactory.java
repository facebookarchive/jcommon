package com.facebook.collections.specialized;

import com.facebook.collections.SetFactory;

import java.util.HashSet;
import java.util.Set;

public class HashSetFactory<T> implements SetFactory<T, Set<T>> {
  @Override
  public Set<T> create() {
    return new HashSet<T>(64);
  }
}
