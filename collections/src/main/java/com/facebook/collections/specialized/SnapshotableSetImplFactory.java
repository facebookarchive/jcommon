package com.facebook.collections.specialized;

import com.facebook.collections.SetFactory;

import java.util.Set;

public class SnapshotableSetImplFactory<T> implements SetFactory<T, SnapshotableSet<T>> {

  private final SetFactory<T, Set<T>> factory;

  public SnapshotableSetImplFactory(SetFactory<T, Set<T>> factory) {
    this.factory = factory;
  }

  @Override
  public SnapshotableSet<T> create() {
    return new SnapshotableSetImpl<T>(factory.create(), this);
  }
}
