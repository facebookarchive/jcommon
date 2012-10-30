package com.facebook.collections.specialized;

import cern.colt.list.LongArrayList;
import cern.colt.map.OpenLongObjectHashMap;

import java.util.AbstractSet;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * uses
 * <p/>
 * http://acs.lbl.gov/software/colt/
 * <p/>
 * to implement a memory efficient hash set of longs
 */
public class ColtLongHashSet extends AbstractSet<Long> implements SnapshotableSet<Long> {
  private static final Object TRUE = new Object();

  private final OpenLongObjectHashMap map;

  private volatile long version = Long.MIN_VALUE;

  ColtLongHashSet(OpenLongObjectHashMap map) {
    this.map = map;
  }

  public ColtLongHashSet(int initialCapacity) {
    this.map = new OpenLongObjectHashMap(initialCapacity);
  }

  @Override
  public synchronized boolean add(Long aLong) {
    version++;

    return map.put(aLong.longValue(), TRUE);
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
      synchronized (ColtLongHashSet.this) {
        return index < mapKeyList.size();
      }
    }

    @Override
    public Long next() {
      synchronized (ColtLongHashSet.this) {
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
      synchronized (ColtLongHashSet.this) {
        if (!canRemove) {
          throw new IllegalStateException(
            "repeated remove() calls or next() not called"
          );
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
    OpenLongObjectHashMap mapCopy = (OpenLongObjectHashMap) map.clone();
    ColtLongHashSet thisCopy = new ColtLongHashSet(mapCopy);

    return thisCopy;
  }

  @Override
  public SnapshotableSet<Long> makeTransientSnapshot() {
    return makeSnapshot();
  }
}
