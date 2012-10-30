package com.facebook.collections.specialized;


import com.facebook.collections.SnapshotProvider;

import java.util.Set;

/**
 * maker interface that is a Set<T> that also implements SnapshotProvider
 * @param <T>
 */
public interface SnapshotableSet<T> 
  extends Set<T>, SnapshotProvider<SnapshotableSet<T>> {
}
