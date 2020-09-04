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

import com.facebook.collections.SetFactory;
import com.facebook.collections.WrappedIterator;
import com.facebook.util.digest.DigestFunction;
import com.facebook.util.serialization.SerDe;
import com.facebook.util.serialization.SerDeException;
import com.google.common.collect.ImmutableSet;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Thread-safe implementation of SampledSet
 * http://algo.inria.fr/flajolet/Publications/Slides/aofa07.pdf section 2.1 Adaptive Sampling
 *
 * @param <T> type of element in the set
 */
// TODO : optimize concurrency if proves to be a bottleneck
public class SampledSetImpl<T> implements SampledSet<T> {

  private final DigestFunction<T> digestFunction;
  private final SetFactory<T, SnapshotableSet<T>> setFactory;
  private final int maxSetSize;
  private final AtomicInteger proposedSize = new AtomicInteger(0);
  // this protects baseSet and currentSampleRate; writeLock is held to
  // safely perform downsampling or downsample + add().  add() and copy
  // operations also need the read lock
  private final ReadWriteLock downSampleLock = new ReentrantReadWriteLock();
  private final AtomicBoolean dirty = new AtomicBoolean(false);

  private volatile SnapshotableSet<T> baseSet;
  // if md5 % sampleRate == 0, we will keep the value
  private volatile int currentSampleRate;

  // ideally, this would be private, but HashDistinctCountAggregation needs it to convert legacy
  // SampledSetImpl<Long> to SampledSetImpl<Integer>
  @Deprecated
  public SampledSetImpl(
      int maxSetSize,
      DigestFunction<T> digestFunction,
      SnapshotableSet<T> baseSet,
      SetFactory<T, SnapshotableSet<T>> setFactory,
      int currentSampleRate) {
    this.maxSetSize = maxSetSize;
    this.digestFunction = digestFunction;
    this.setFactory = setFactory;
    this.baseSet = baseSet;
    this.currentSampleRate = currentSampleRate;
    proposedSize.set(baseSet.size());
  }

  public SampledSetImpl(
      int maxSetSize,
      DigestFunction<T> digestFunction,
      SetFactory<T, SnapshotableSet<T>> setFactory) {
    this(maxSetSize, digestFunction, setFactory.create(), setFactory, 1);
  }

  @Override
  public boolean add(T element) {
    // algorithm:
    //   1. store sample rate
    //   2. check if element is in in sample
    //   3. increment proposed size
    //   4. if >= max, grab writeLock, downsample, re-check if in sample,
    //      and add to set if in sample;
    //   5. if < max, grab read lock. if sample rate changed, re-check if in
    //      sample. add if still in sample or rate hasn't changed
    //   6. either case, decrement proposed size if element ends up
    //     not being added
    //   7. release read or write lock
    boolean returnValue = false;
    long elementDigest = digestFunction.computeDigest(element);
    int sampleRateSnapshot = currentSampleRate;

    // is this value in our current sample
    if (inSample(elementDigest, sampleRateSnapshot)) {
      // check if we will exceed the max size
      if (proposedSize.incrementAndGet() > maxSetSize) {
        // then acquire writeLock and perform downsample + add while holding
        // the writeLock
        downSampleLock.writeLock().lock();

        try {
          if (inSample(elementDigest, currentSampleRate) && !baseSet.contains(element)) {
            // adding something new
            downSample();
            // need to add while we hold the lock to guarantee we don't exceed
            // the max
            if (inSample(elementDigest, currentSampleRate)) {
              returnValue = baseSet.add(element);
            }
          }
        } finally {
          if (!returnValue) {
            proposedSize.decrementAndGet();
          }

          downSampleLock.writeLock().unlock();
        }
      } else {
        // we won't exceed max size; make sure the sample rate holds constant
        // and add to the set
        downSampleLock.readLock().lock();

        try {
          // we only need to check if this element is in the sample again if
          // currentSampleRate has changed
          if (currentSampleRate == sampleRateSnapshot
              || inSample(elementDigest, currentSampleRate)) {
            returnValue = baseSet.add(element);
          }
        } finally {
          if (!returnValue) {
            proposedSize.decrementAndGet();
          }

          downSampleLock.readLock().unlock();
        }
      }
    }

    if (returnValue) {
      dirty.set(true);
    }

    return returnValue;
  }

  private boolean inSample(long digest, int sampleRate) {
    return digest % sampleRate == 0;
  }

  private void downSample() {
    // very unlikely, but possible that increasing the sample rate won't
    // remove a single value; so do this in a loop
    int removed = 0;

    while (baseSet.size() >= maxSetSize) {
      currentSampleRate <<= 1;
      assert (currentSampleRate > 1);

      removed += downSampleAtRate(currentSampleRate, baseSet);
    }

    if (removed > 0) {
      proposedSize.addAndGet(-removed);
    }
  }

  private int downSampleAtRate(int sampleRate, Set<T> set) {
    int removed = 0;
    Iterator<T> iterator = set.iterator();

    while (iterator.hasNext()) {
      T value = iterator.next();

      if (!inSample(digestFunction.computeDigest(value), sampleRate)) {
        iterator.remove();
        removed++;
      }
    }

    return removed;
  }

  private SnapshotableSet<T> copyAtRate(int sampleRate) {
    if (sampleRate <= currentSampleRate) {
      // make a fast copy
      return baseSet.makeSnapshot();
    } else {
      // make a fast-copy and down-sample--faster to remove elements
      // than re-add them
      SnapshotableSet<T> target = baseSet.makeSnapshot();

      downSampleAtRate(sampleRate, target);

      return target;
    }
  }

  @Override
  public int getMaxSetSize() {
    return maxSetSize;
  }

  @Override
  public int getScaledSize() {
    return baseSet.size() * currentSampleRate;
  }

  @Override
  public int getSampleRate() {
    return currentSampleRate;
  }

  @Override
  public int getSize() {
    return baseSet.size();
  }

  @Override
  public Set<T> getEntries() {
    return ImmutableSet.copyOf(baseSet);
  }

  @Override
  public SampledSetSnapshot<T> sampleAt(int rate) {
    SnapshotableSet<T> setCopy;
    int setCopySampleRate;

    // grab this lock to make sure we have a consistent view of the sample rate
    // and the set;
    downSampleLock.readLock().lock();

    try {
      setCopySampleRate = Math.max(rate, currentSampleRate);
      setCopy = copyAtRate(setCopySampleRate);
    } finally {
      downSampleLock.readLock().unlock();
    }

    return new SampledSetSnapshot<>(setCopySampleRate, maxSetSize, setCopy);
  }

  @Override
  public SampledSet<T> merge(SampledSet<T> sampledSet) {
    // fast-copy of ourself for merging
    SampledSet<T> mergedSampleSet = this.makeSnapshot();
    // now merge sampledSet into the copy
    mergedSampleSet.mergeInPlaceWith(sampledSet);
    // clear the changed status
    mergedSampleSet.hasChanged();

    return mergedSampleSet;
  }

  @Override
  public boolean mergeInPlaceWith(SampledSet<T> sampledSet) {
    boolean changed = false;
    // take a snapshot of the other set at our sample rate. Note that it
    // only will use this rate if it is higher than its current sample rate
    SampledSetSnapshot<T> snapshot = sampledSet.sampleAt(currentSampleRate);
    // grab our downSampleLock.writeLock to make sure the sampleRate doesn't
    // change while we work
    downSampleLock.writeLock().lock();

    try {
      // shortcut for fast copy: we're empty, the snapshot's sample rate is
      // compatible with our sampleRate, and fits within our maxSize
      if (currentSampleRate <= snapshot.getSampleRate()
          && baseSet.isEmpty()
          && maxSetSize >= snapshot.getElements().size()) {
        // copy the set, current sample size, and increment the version
        baseSet = snapshot.getElements();
        currentSampleRate = snapshot.getSampleRate();
        proposedSize.set(baseSet.size());
        dirty.set(true);

        return true;
      } else if (!snapshot.getElements().isEmpty()
          && snapshot.getSampleRate() > currentSampleRate) {
        // only downsample ourself if there are actually elements in the other
        // set to merge into ourself
        int removed = downSampleAtRate(snapshot.getSampleRate(), baseSet);
        if (removed > 0) {
          changed = true;
          proposedSize.addAndGet(-removed);
        }

        currentSampleRate = snapshot.getSampleRate();
      }
    } finally {
      downSampleLock.writeLock().unlock();
    }

    // safe to do this outside the lock since we know our sampleRate is
    // at least as high as that of the elements we are adding
    for (T element : snapshot.getElements()) {
      if (add(element)) {
        changed = true;
      }
    }

    if (changed) {
      dirty.set(true);
    }

    return changed;
  }

  @Override
  public boolean hasChanged() {
    return dirty.getAndSet(false);
  }

  @Override
  public Iterator<T> iterator() {
    return new WrappedIterator<T>(baseSet.iterator()) {
      @Override
      public void remove() {
        super.remove();
        dirty.set(true);
      }
    };
  }

  @Override
  public int size() {
    return getSize();
  }

  @Override
  public boolean isEmpty() {
    return baseSet.isEmpty();
  }

  @Override
  public boolean contains(Object o) {
    return baseSet.contains(o);
  }

  @Override
  public Object[] toArray() {
    return baseSet.toArray();
  }

  @Override
  public <V> V[] toArray(V[] a) {
    return baseSet.toArray(a);
  }

  @Override
  public boolean remove(Object o) {
    if (baseSet.remove(o)) {
      dirty.set(true);
      return true;
    }

    return false;
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    return baseSet.containsAll(c);
  }

  @Override
  public boolean addAll(Collection<? extends T> c) {
    boolean added = false;

    for (T item : c) {
      if (add(item)) {
        added = true;
      }
    }

    if (added) {
      dirty.set(true);
    }

    return added;
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    if (baseSet.retainAll(c)) {
      dirty.set(true);

      return true;
    }

    return false;
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    if (baseSet.removeAll(c)) {
      dirty.set(true);

      return true;
    }

    return false;
  }

  @Override
  public void clear() {
    baseSet.clear();

    dirty.set(true);
  }

  @Override
  public SampledSet<T> makeSnapshot() {
    return new SampledSetImpl<>(
        maxSetSize, digestFunction, baseSet.makeSnapshot(), setFactory, currentSampleRate);
  }

  @Override
  public SampledSet<T> makeTransientSnapshot() {
    SnapshotableSetImplFactory<T> cpuEfficientHashSetFactory =
        new SnapshotableSetImplFactory<>(new HashSetFactory<>());
    SnapshotableSet<T> cpuEfficientHashSet = baseSet.makeTransientSnapshot();

    return new SampledSetImpl<>(
        maxSetSize,
        digestFunction,
        cpuEfficientHashSet,
        cpuEfficientHashSetFactory,
        currentSampleRate);
  }

  @Override
  public boolean equals(Object o) {
    downSampleLock.writeLock().lock();

    try {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      SampledSetImpl<T> that = (SampledSetImpl<T>) o;

      if (currentSampleRate != that.currentSampleRate) {
        return false;
      }
      if (maxSetSize != that.maxSetSize) {
        return false;
      }
      if (!Objects.equals(baseSet, that.baseSet)) {
        return false;
      }

      return true;
    } finally {
      downSampleLock.writeLock().unlock();
    }
  }

  @Override
  public int hashCode() {
    downSampleLock.writeLock().lock();

    try {
      int result = baseSet != null ? baseSet.hashCode() : 0;

      result = 31 * result + maxSetSize;
      result = 31 * result + currentSampleRate;

      return result;
    } finally {
      downSampleLock.writeLock().unlock();
    }
  }

  public static class SerDeImpl<T> implements SerDe<SampledSet<T>> {
    private final SetFactory<T, SnapshotableSet<T>> setFactory;
    private final DigestFunction<T> digestFunction;
    private final SerDe<T> elementSerDe;

    public SerDeImpl(
        SetFactory<T, SnapshotableSet<T>> setFactory,
        DigestFunction<T> digestFunction,
        SerDe<T> elementSerDe) {
      this.setFactory = setFactory;
      this.digestFunction = digestFunction;
      this.elementSerDe = elementSerDe;
    }

    @Override
    public SampledSet<T> deserialize(DataInput in) throws SerDeException {
      try {
        int maxSize = in.readInt();
        int sampleRate = in.readInt();
        int numElements = in.readInt();

        SnapshotableSet<T> baseSet = setFactory.create();

        for (int i = 0; i < numElements; i++) {
          baseSet.add(elementSerDe.deserialize(in));
        }

        SampledSet<T> sampledSet =
            new SampledSetImpl<>(maxSize, digestFunction, baseSet, setFactory, sampleRate);

        return sampledSet;
      } catch (IOException e) {
        throw new SerDeException(e);
      }
    }

    @Override
    public void serialize(SampledSet<T> value, DataOutput out) throws SerDeException {
      try {
        // sampling at 0 will make a copy at the existing sample rate (since the
        // rate is only used if it is larger than the existing rate)
        SampledSetSnapshot<T> snapshot = value.sampleAt(0);
        Set<T> elements = snapshot.getElements();

        out.writeInt(snapshot.getMaxSetSize());
        out.writeInt(snapshot.getSampleRate());
        out.writeInt(elements.size());

        for (T element : elements) {
          elementSerDe.serialize(element, out);
        }

      } catch (IOException e) {
        throw new SerDeException(e);
      }
    }
  }
}
