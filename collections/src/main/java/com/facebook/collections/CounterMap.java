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

import com.google.common.collect.Iterators;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * maps keys of type K to a long.  Thread-safe and cleans up keys when
 * the count reaches 0
 *
 * @param <K> key type for the map
 */
public class CounterMap<K> implements Iterable<Map.Entry<K, Long>> {
  private static final ValueComparableAtomicLong ZERO = new ValueComparableAtomicLong(0);

  private final ConcurrentMap<K, ValueComparableAtomicLong> counters = new ConcurrentHashMap<>();
  private final ReadWriteLock removalLock = new ReentrantReadWriteLock();

  /**
   * adds a value to key; will create a counter if it doesn't exist already.
   * conversely, since 0 is the value returned for keys not present, if the
   * counter value reaches 0, it is removed
   *
   * @param key   - key to add the delta to
   * @param delta - positive/negative amount to increment a counter
   * @return the new value after updating
   */
  public long addAndGet(K key, long delta) {
    long retVal;

    // ensure that no key is removed while we are updating the counter
    removalLock.readLock().lock();
    try {
      retVal = getCounter(key).addAndGet(delta);
    } finally {
      removalLock.readLock().unlock();
    }

    if (retVal == 0) {
      tryCleanup(key);
    }

    return retVal;
  }

  public long size() {
    return counters.size();
  }

  /**
   * adds a value to key; will create a counter if it doesn't exist already.
   * conversely, since 0 is the value returned for keys not present, if the
   * counter value reaches 0, it is removed
   *
   * @param key   - key to add the delta to
   * @param delta - positive/negative amount to increment a counter
   * @return the old value before updating
   */
  public long getAndAdd(K key, long delta) {
    long retVal;

    // ensure that no key is removed while we are updating the counter
    removalLock.readLock().lock();
    try {
      retVal = getCounter(key).getAndAdd(delta);
    } finally {
      removalLock.readLock().unlock();
    }

    if ((retVal + delta) == 0) {
      tryCleanup(key);
    }

    return retVal;
  }

  private ValueComparableAtomicLong getCounter(K key) {
    ValueComparableAtomicLong counter = counters.get(key);
    if (counter == null) {
      ValueComparableAtomicLong newCounter = new ValueComparableAtomicLong(0);
      ValueComparableAtomicLong oldCounter = counters.putIfAbsent(key, newCounter);
      counter = (oldCounter == null) ? newCounter : oldCounter;
    }
    return counter;
  }

  private void tryCleanup(K key) {
    removalLock.writeLock().lock();
    try {
      counters.remove(key, ZERO);
    } finally {
      removalLock.writeLock().unlock();
    }
  }

  /**
   * @param key
   * @return value removed if present, null otherwise
   */
  public AtomicLong remove(K key) {
    // no locking, this is an unconditional remove
    ValueComparableAtomicLong removedCounter = counters.remove(key);
    return removedCounter == null ? null : removedCounter.getValue();
  }

  /**
   * @param key
   * @return value of a counter.  Returns 0 if not present
   */
  public long get(K key) {
    ValueComparableAtomicLong counter = counters.get(key);

    if (counter == null) {
      return 0;
    }

    return counter.get();
  }

  /**
   * if no value has been written for a counter, this will seed it
   *
   * @param key
   * @param value
   * @return
   */
  public long tryInitializeCounter(K key, long value) {
    removalLock.readLock().lock();

    try {
      ValueComparableAtomicLong counter = getCounter(key);

      counter.compareAndSet(0L, value);

      return counter.get();
    } finally {
      removalLock.readLock().unlock();
    }
  }

  public long trySetCounter(K key, long value) {
    removalLock.readLock().lock();

    try {
      ValueComparableAtomicLong counter = getCounter(key);

      counter.compareAndSet(counter.get(), value);

      return counter.get();
    } finally {
      removalLock.readLock().unlock();
    }
  }

  @Override
  public Iterator<Map.Entry<K, Long>> iterator() {
    return Iterators.unmodifiableIterator(
      new TranslatingIterator<>(
        input -> new AbstractMap.SimpleImmutableEntry<>(input.getKey(), input.getValue().get()),
        counters.entrySet().iterator()
      )
    );
  }

  private static class ValueComparableAtomicLong {
    private final AtomicLong value;

    private ValueComparableAtomicLong(AtomicLong value) {
      this.value = value;
    }

    private ValueComparableAtomicLong(long value) {
      this(new AtomicLong(value));
    }

    private AtomicLong getValue() {
      return value;
    }

    private long get() {
      return value.get();
    }

    private long addAndGet(long delta) {
      return value.addAndGet(delta);
    }

    private long getAndAdd(long delta) {
      return value.getAndAdd(delta);
    }

    public boolean compareAndSet(long expect, long update) {
      return value.compareAndSet(expect, update);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) { return true; }
      if (o == null || getClass() != o.getClass()) { return false; }
      ValueComparableAtomicLong that = (ValueComparableAtomicLong) o;
      return value.get() == that.value.get();
    }

    @Override
    public int hashCode() {
      return Long.hashCode(value.get());
    }
  }
}
