/*
 * Copyright (C) 2012 Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.collections;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Highly concurrent (no limit, uses ReadLock) for adds.  Removes hold
 * the write lock if the set is found to be empty and an attempt to remove it
 * from the map is made 
 *
 * @param <K> type for the key
 * @param <V> type of the elements in the set
 */
public class SetMapImpl<K, V, S extends Set<V>> implements SetMap<K,V,S> {
  private final ConcurrentMap<K, S> sets =
    new ConcurrentHashMap<K, S>();
  private final ReadWriteLock removalLock = new ReentrantReadWriteLock();
  private final SetFactory<V, S> setFactory;

  public SetMapImpl(SetFactory<V, S> setFactory) {
    this.setFactory = setFactory;
  }

  @Override
  public boolean add(K key, V item) {
    // we have to make sure that the set isn't removed while we add to it
    removalLock.readLock().lock();

    try {
      S set = sets.get(key);

      if (set == null) {
        set = setFactory.create();
  
        S existingSet = sets.putIfAbsent(key, set);
  
        if (existingSet != null) {
          set = existingSet;
        }
      }

      return set.add(item);
    } finally {
      removalLock.readLock().unlock();
    }
  }

  @Override
  public S removeSet(K key) {
    // unconditional remove, so no lock held
    return sets.remove(key);
  }

  @Override
  public boolean remove(K key, V item) {
    S set = sets.get(key);

    if (set == null) {
      return false;
    }

    // did we remove the value from the underlying set
    boolean removed = set.remove(item);

    if (set.isEmpty()) {
      // conditional remove requires that no elements be added to the set
      //during this time
      removalLock.writeLock().lock();
      try {
        sets.remove(key, Collections.EMPTY_SET);
      } finally {
        removalLock.writeLock().unlock();
      }
    }

    return removed;
  }

  @Override
  public S get(K key) {
    return sets.get(key);
  }

  @Override
  public void clear() {
    sets.clear();
  }

  @Override
  public Iterator<Map.Entry<K, S>> iterator() {
    return sets.entrySet().iterator();
  }
}
