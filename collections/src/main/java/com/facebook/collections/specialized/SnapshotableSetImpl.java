package com.facebook.collections.specialized;

import com.facebook.collections.SetFactory;
import com.facebook.collections.WrappedSet;

import java.util.Set;

/**
 * basic utility class that converts a Set<T> and the appropriate SetFactory
 * into a SnapshotableSet
 * @param <T>
 */
public class SnapshotableSetImpl<T>
  extends WrappedSet<T> implements SnapshotableSet<T> {

  private final SetFactory<T, SnapshotableSet<T>> setFactory;

  public SnapshotableSetImpl(
    Set<T> delegate, SetFactory<T, SnapshotableSet<T>> setFactory
  ) {
    super(delegate);
    this.setFactory = setFactory;
  }

  @Override
  public SnapshotableSet<T> makeSnapshot() {
    SnapshotableSet<T> setCopy = setFactory.create();

    // assumes that delegate is either already thread-safe (and therefore
    // "synchronized" is unnecessary) or that it uses the object's monitor to
    // guard access to mutator methods (e.g. Collections.synchronizedSet())
    Set<T> delegate = getDelegate();
    synchronized (delegate) {
      setCopy.addAll(delegate);
    }

    return setCopy;
  }

  @Override
  public SnapshotableSet<T> makeTransientSnapshot() {
    return makeSnapshot();
  }
}
