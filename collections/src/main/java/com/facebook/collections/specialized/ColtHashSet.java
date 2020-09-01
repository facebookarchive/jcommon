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
package com.facebook.collections.specialized;

import cern.colt.list.LongArrayList;
import cern.colt.map.OpenLongObjectHashMap;
import java.util.AbstractSet;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * uses
 *
 * <p>http://acs.lbl.gov/software/colt/
 *
 * <p>to implement a memory efficient hash set
 */
public class ColtHashSet extends AbstractSet<Long> implements SnapshotableSet<Long> {
  private static final Object TRUE = new Object();

  private final OpenLongObjectHashMap map;

  private volatile long version = Long.MIN_VALUE;

  public ColtHashSet(int initialCapacity, double minLoadFactor, double maxLoadFactor) {
    map = new OpenLongObjectHashMap(initialCapacity, minLoadFactor, maxLoadFactor);
  }

  public ColtHashSet(int initialCapacity) {
    map = new OpenLongObjectHashMap(initialCapacity);
  }

  @Override
  public synchronized boolean add(Long aLong) {
    version++;

    return map.put(aLong, TRUE);
  }

  @Override
  public Iterator<Long> iterator() {
    return new Iter();
  }

  @Override
  public synchronized int size() {
    return map.size();
  }

  private class Iter implements Iterator<Long> {
    private final LongArrayList mapKeyList = map.keys();

    private int index = 0;
    private long versionSnapshot = version;
    private boolean canRemove = false;

    @Override
    public boolean hasNext() {
      synchronized (ColtHashSet.this) {
        return index < mapKeyList.size();
      }
    }

    @Override
    public Long next() {
      synchronized (ColtHashSet.this) {
        if (versionSnapshot != version) {
          throw new ConcurrentModificationException();
        }

        if (index >= mapKeyList.size()) {
          throw new NoSuchElementException();
        }

        long value = mapKeyList.getQuick(index);

        index++;
        canRemove = true;

        return value;
      }
    }

    @Override
    public void remove() {
      synchronized (ColtHashSet.this) {
        if (!canRemove) {
          throw new IllegalStateException("repeated remove() calls or next() not called");
        }

        map.removeKey(mapKeyList.get(index - 1));
        canRemove = false;
        version++;
        versionSnapshot = version;
      }
    }
  }

  @Override
  public SnapshotableSet<Long> makeSnapshot() {
    // map.clone() makes a proper deep copy
    return (SnapshotableSet<Long>) map.clone();
  }

  @Override
  public SnapshotableSet<Long> makeTransientSnapshot() {
    return makeSnapshot();
  }
}
