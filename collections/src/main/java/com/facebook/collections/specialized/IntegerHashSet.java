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

import com.facebook.collections.Trackable;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import gnu.trove.impl.sync.TSynchronizedIntSet;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import javax.annotation.concurrent.GuardedBy;

/** making this take a Long for compatibility, but operates on integers */
public class IntegerHashSet implements SnapshotableSet<Long>, Trackable {
  private static final float MAX_LOAD_FACTOR = 2 / 3.0f;

  private final TIntSet set;
  private final Object mutex = new Object();
  private final int maxCapacity;

  @GuardedBy("mutex")
  private volatile boolean hasChanged;

  public IntegerHashSet(int initialCapacity, int maxCapacity) {
    Preconditions.checkArgument(
        initialCapacity <= maxCapacity,
        "initial capacity of %s cannot be larger than max of %s",
        initialCapacity,
        maxCapacity);
    set = new TSynchronizedIntSet(new TIntHashSet(initialCapacity, MAX_LOAD_FACTOR, -1), mutex);
    this.maxCapacity = maxCapacity;
  }

  public IntegerHashSet(int maxCapacity) {
    this(Math.max(maxCapacity / 16, 1), maxCapacity);
  }

  private IntegerHashSet(IntegerHashSet set) {
    this.set = new TSynchronizedIntSet(new TIntHashSet(set.set), mutex);
    this.maxCapacity = set.maxCapacity;
  }

  @Override
  public boolean contains(Object value) {
    if (value instanceof Integer || value instanceof Long) {
      synchronized (mutex) {
        return set.contains(((Number) value).intValue());
      }
    }

    return false;
  }

  @VisibleForTesting
  boolean add(Integer value) {
    return add(value.longValue());
  }

  @Override
  public boolean add(Long value) {
    synchronized (mutex) {
      // there must be room for at least one element, even adding duplicates; the point is, no
      // add() should be called once we're maxed out.
      Preconditions.checkState(
          set.size() < maxCapacity,
          "set is size %s which means we're full, but someone's calling add. Why?",
          set.size());

      if (set.add(value.intValue())) {
        hasChanged = true;

        return true;
      }
    }

    return false;
  }

  @Override
  public boolean remove(Object value) {
    if (value instanceof Integer || value instanceof Long) {
      synchronized (mutex) {
        if (set.remove(((Number) value).intValue())) {
          hasChanged = true;
        }
      }
    }

    return hasChanged;
  }

  @Override
  public int size() {
    return set.size();
  }

  @Override
  public boolean isEmpty() {
    return set.isEmpty();
  }

  @Override
  public Iterator<Long> iterator() {
    synchronized (mutex) {
      return new Iterator<Long>() {
        private TIntIterator iterator = set.iterator();

        @Override
        public boolean hasNext() {
          return iterator.hasNext();
        }

        @Override
        @SuppressWarnings("IteratorNextCanNotThrowNoSuchElementException")
        public Long next() throws NoSuchElementException {
          return (long) iterator.next();
        }

        @Override
        public void remove() {
          synchronized (mutex) {
            iterator.remove();
            hasChanged = true;
          }
        }
      };
    }
  }

  @Override
  public Object[] toArray() {
    int[] ints = set.toArray();
    Object[] result = new Object[ints.length];

    for (int i = 0; i < result.length; i++) {
      result[i] = ints[i];
    }

    return result;
  }

  @Override
  public <T> T[] toArray(T[] result) {
    int[] ints = set.toArray();

    if (result.length < ints.length) {
      //noinspection unchecked
      result = (T[]) Array.newInstance(result.getClass().getComponentType(), ints.length);
    }

    for (int i = 0; i < result.length; i++) {
      result[i] = (T) Integer.valueOf(ints[i]);
    }

    return result;
  }

  @Override
  public boolean containsAll(Collection<?> values) {
    return set.containsAll(values);
  }

  @Override
  public boolean addAll(Collection<? extends Long> values) {
    boolean retVal = false;

    for (Long value : values) {
      Preconditions.checkState(
          set.size() < maxCapacity,
          "set is size %s which means we're full, but someone's calling add. Why?",
          set.size());
      retVal |= set.add(value.intValue());
    }

    return retVal;
  }

  @Override
  public boolean retainAll(Collection<?> values) {
    boolean methodHasChanged;

    synchronized (mutex) {
      methodHasChanged = set.retainAll(values);
      hasChanged |= methodHasChanged;
    }
    return methodHasChanged;
  }

  @Override
  public boolean removeAll(Collection<?> values) {
    boolean methodHasChanged;

    synchronized (mutex) {
      methodHasChanged = set.removeAll(values);
      hasChanged |= methodHasChanged;
    }

    return methodHasChanged;
  }

  @Override
  public void clear() {
    synchronized (mutex) {
      if (!set.isEmpty()) {
        set.clear();
        hasChanged = true;
      }
    }
  }

  @Override
  public SnapshotableSet<Long> makeSnapshot() {
    synchronized (mutex) {
      return new IntegerHashSet(this);
    }
  }

  @Override
  public SnapshotableSet<Long> makeTransientSnapshot() {
    return makeSnapshot();
  }

  @Override
  public boolean hasChanged() {
    synchronized (mutex) {
      try {
        return hasChanged;
      } finally {
        hasChanged = false;
      }
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o instanceof IntegerHashSet) {
      IntegerHashSet integerHashSet = (IntegerHashSet) o;

      return !(!Objects.equals(set, integerHashSet.set));
    } else if (o instanceof Set) {
      Set otherSet = (Set) o;

      return (this.set != null && this.set.size() == otherSet.size() && this.containsAll(otherSet));
    } else {
      return this.set == null && o == null;
    }
  }

  @Override
  public int hashCode() {
    int result = set != null ? set.hashCode() : 0;
    result = 31 * result + (mutex != null ? mutex.hashCode() : 0);
    result = 31 * result + (hasChanged ? 1 : 0);
    return result;
  }
}
