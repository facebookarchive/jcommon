package com.facebook.collections.specialized;

import com.facebook.collections.SetFactory;
import com.facebook.collections.SetMap;
import com.facebook.collections.SetMapImpl;
import com.facebook.collections.Trackable;
import com.facebook.collections.WrappedIterator;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * decorates a SetMap so that a caller can tell if the data structure
 * has changed.
 *
 * NOTE: if a caller modifies the set directly:
 *   Set<V> set = setMap.get(key);
 *   set.add(key);
 *
 * This will not be reflected in the version
 *
 * @param <K>
 * @param <V>
 * @param <S>
 */
public class TrackableSetMap<K, V, S extends Set<V>>
  implements SetMap<K,V,S>, Trackable {

  private final SetMap<K,V,S> delegate;
  private final AtomicBoolean dirty = new AtomicBoolean(false);

  public TrackableSetMap(SetMap<K, V, S> delegate) {
    this.delegate = delegate;
  }

  public TrackableSetMap(SetFactory<V, S> setFactory) {
    delegate = new SetMapImpl<K, V, S>(setFactory);
  }

  @Override
  public boolean add(K key, V item) {
    if (delegate.add(key, item)) {
      dirty.set(true);

      return true;
    }

    return false;
  }

  @Override
  public S removeSet(K key) {
    S value = delegate.removeSet(key);

    if (value != null) {
      dirty.set(true);
    }

    return value;
  }

  @Override
  public boolean remove(K key, V item) {
    if (delegate.remove(key, item)) {
      dirty.set(true);

      return true;
    }

    return false;
  }

  @Override
  public boolean hasChanged() {
    return dirty.getAndSet(false);
  }

  @Override
  public S get(K key) {
    return delegate.get(key);
  }

  @Override
  public Iterator<Map.Entry<K, S>> iterator() {
    return new WrappedIterator<Map.Entry<K, S>>(delegate.iterator()) {
      @Override
      public void remove() {
        super.remove();
        dirty.set(true);
      }
    };
  }
}
