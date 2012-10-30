package com.facebook.collections.specialized;

import com.facebook.collections.SetFactory;

import java.util.Collections;
import java.util.Set;

public class SynchronizedSetFactory<T> implements SetFactory<T, Set<T>> {
  private final SetFactory<T, Set<T>> factory;

  public SynchronizedSetFactory(SetFactory<T, Set<T>> factory) {
    this.factory = factory;
  }

  @Override
  public Set<T> create() {
    return Collections.<T>synchronizedSet(factory.create());
  }
}
