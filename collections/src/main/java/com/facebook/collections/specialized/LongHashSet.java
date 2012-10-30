package com.facebook.collections.specialized;

import com.facebook.collections.Trackable;
import com.facebook.collectionsbase.Mapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

import javax.annotation.concurrent.GuardedBy;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * stores a set of non-negative long values using a fixed-size array.
 *
 * this class is thread-safe, and allows decent parallelism using a
 * ReadWriteLock.  The iterator is thread-safe, but can still throw
 * a ConcurrentModificationException (TODO: can probably fix this)
 */
public class LongHashSet implements SnapshotableSet<Long>, Trackable {
  // these are values for slots
  private static final long INITIAL_EMPTY = -1;
  private static final long REMOVED_EMPTY = -2;
  // this is a reserved return value
  private static final int FULL_SET = -3;
  private static final float MIN_LOAD_FACTOR = 2 / 3.0f;
  private static final float MAX_LOAD_FACTOR = 9 / 10.0f;

  private final Mapper<Long, Integer> hashFunction;
  // guards all mutations to values
  private final ReadWriteLock lock = new ReentrantReadWriteLock();
  private final AtomicLong version = new AtomicLong(0);
  private long lastCheckedVersion = 0;
  @GuardedBy("lock")
  private volatile long[] values;
  private AtomicInteger size = new AtomicInteger(0);
  private final int maxCapacity;

  public LongHashSet(
    int initialCapacity, int maxCapacity, Mapper<Long, Integer> hashFunction
  ) {
    Preconditions.checkArgument(
      initialCapacity <= maxCapacity,
      "initial capacity of %s cannot be larger than max of %s",
      initialCapacity,
      maxCapacity
    );
    this.maxCapacity = maxCapacity;
    initArrays(initialCapacity);
    this.hashFunction = hashFunction;
  }

  private void initArrays(int capacity) {
    values = new long[capacity];
    Arrays.fill(values, INITIAL_EMPTY);
  }

  /**
   * creates a bounded set with an initial capacity
   *
   * @param initialCapacity
   * @param maxCapacity
   */
  public LongHashSet(int initialCapacity, int maxCapacity) {
    this(
      initialCapacity,
      maxCapacity,
      new Mapper<Long, Integer>() {
        @Override
        public Integer map(Long input) {
          return (int) (input ^ (input >>> 32));
        }
      }
    );
  }

  private void resize() {
    if (values.length == maxCapacity) {
      throw new IllegalStateException(
        String.format(
          "cannot resize: max capacity of %d already reached", maxCapacity
        )
      );
    }

    int desiredSize = (int) (values.length / MIN_LOAD_FACTOR);
    int newSize = Math.min(desiredSize, maxCapacity);

    long[] oldValues = values;
    values = new long[newSize];
    internalClear();

    for (long value : oldValues) {
      if (value >= 0) {
        internalAdd(value);
      }
    }
  }

  private int hashValueOf(Long aLong) {
    return Math.abs(hashFunction.map(aLong)) % values.length;
  }

  /**
   * returns location of either the value, or the next location to place
   * the value
   *
   * @param aLong
   * @return if aLong is present in the set, returns the index of it; if it
   *         is not present, returns the index of where it may be placed,
   *         or FULL_SET(-2) if the set is full
   */
  private int findLocationOf(long aLong) {
    int index = hashValueOf(aLong);
    int firstEmptyIndex = -1;
    int total = 0;

    // stop if we find aLong or an empty slot
    while (values[index] != aLong) {
      if (values[index] == INITIAL_EMPTY) {
        return index;
      }

      if (isEmptySlot(index)) {
        if (firstEmptyIndex == -1) {
          firstEmptyIndex = index;
        }
      }

      total++;

      // case that we've seen every slot
      if (total == values.length) {
        if (firstEmptyIndex == -1) {
          return FULL_SET;
        } else {
          return firstEmptyIndex;
        }
      }

      index = (index + 1) % values.length;
    }

    // this means we found aLong
    return index;
  }

  private boolean isEmptySlot(int index) {
    return values[index] < 0;
  }

  private void validateArgument(Long aLong) {
    if (aLong < 0) {
      throw new IllegalArgumentException(
        String.format(
          "only non-negative integers are allowed (tried to use %d)", aLong
        )
      );
    }
  }

  @Override
  public int size() {
    return size.get();
  }

  @Override
  public boolean isEmpty() {
    return size.get() == 0;
  }

  @Override
  public boolean contains(Object o) {
    if (!(o instanceof Long)) {
      throw new IllegalArgumentException("type of long required");
    }

    int index;

    lock.readLock().lock();

    try {
      index = findLocationOf((Long) o);

      return index != FULL_SET && !isEmptySlot(index);
    } finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public Iterator<Long> iterator() {
    final AtomicLong snapshotVersion = new AtomicLong(version.get());
    final long sizeSnapshot = size.get();

    return new Iterator<Long>() {
      // invariant: location < values.length. This holds because we
      // require that the set does not change while we iterator. location
      // is only incremented in next() which checks the bound. Remove
      private int location = -1;
      private int visited = 0;
      private boolean canRemove = false;

      @Override
      public boolean hasNext() {
        lock.readLock().lock();

        try {
          if (version.get() != snapshotVersion.get()) {
            throw new ConcurrentModificationException();
          }

          return visited < sizeSnapshot;
        } finally {
          lock.readLock().unlock();
        }
      }

      @Override
      public Long next() {
        lock.readLock().lock();

        try {
          if (version.get() != snapshotVersion.get()) {
            throw new ConcurrentModificationException();
          }

          if (location >= values.length) {
            throw new NoSuchElementException();
          }

          do {
            location++;

            if (location >= values.length) {
              throw new NoSuchElementException();
            }
          } while (isEmptySlot(location));

          visited++;
          canRemove = true;

          //noinspection unchecked
          return values[location];
        } finally {
          lock.readLock().unlock();
        }
      }

      @Override
      public void remove() {
        lock.writeLock().lock();

        try {
          if (!canRemove) {
            // TODO : clean this up?
            throw new IllegalStateException(
              "repeated remove() calls or next() not called"
            );
          }

          // this works because we verify the set hasn't changed and
          // then update the snapshotVersion to a new one
          if (version.get() != snapshotVersion.get()) {
            throw new ConcurrentModificationException();
          }

          values[location] = REMOVED_EMPTY;
          size.decrementAndGet();
          // update the snapshotVersion and also set the valid snapshotVersion to check
          // for changes against
          snapshotVersion.set(version.incrementAndGet());
          canRemove = false;
        } finally {
          lock.writeLock().unlock();
        }
      }
    };
  }

  @Override
  public Object[] toArray() {
    return toArray(new Object[size.get()]);
  }

  @Override
  public <T> T[] toArray(T[] a) {
    if (!a.getClass().getComponentType().isAssignableFrom(Long.class)) {
      throw new ArrayStoreException("array must be of type Long");
    }

    T[] result;

    lock.readLock().lock();

    try {
      if (a.length >= size.get()) {
        result = a;
      } else {
        result = (T[]) java.lang.reflect.Array
          .newInstance(a.getClass().getComponentType(), size.get());
      }

      int i = 0;

      for (Object value : this) {
        result[i++] = (T) value;
      }
    } finally {
      lock.readLock().unlock();
    }

    return result;
  }

  @VisibleForTesting
  boolean add(Integer anInteger) {
    return add(anInteger.longValue());
  }

  @Override
  public boolean add(Long aLong) {
    validateArgument(aLong);

    lock.writeLock().lock();

    try {
      int maxUsedBuckets = (int) (MAX_LOAD_FACTOR * values.length);
      // resize if we are too full and we can add buckets
      if (size.get() > maxUsedBuckets && values.length < maxCapacity) {
        resize();
      }

      if (internalAdd(aLong)) {
        version.incrementAndGet();

        return true;
      } else {
        return false;
      }
    } finally {
      lock.writeLock().unlock();
    }
  }

  private boolean internalAdd(long value) {
    int index = findLocationOf(value);

    if (index == FULL_SET) {
      throw new IllegalStateException(
        String.format(
          "set is full with %d elements, cannot add more",
          values.length
        )
      );
    }

    if (isEmptySlot(index)) {
      values[index] = value;
      size.incrementAndGet();

      return true;
    } else {
      return false;
    }
  }

  @Override
  public boolean remove(Object o) {
    if (!(o instanceof Long)) {
      throw new IllegalArgumentException("type of long required");
    }

    validateArgument((Long) o);

    lock.writeLock().lock();

    try {
      int index = findLocationOf((Long) o);

      if (index != FULL_SET && !isEmptySlot(index)) {
        values[index] = REMOVED_EMPTY;
        version.incrementAndGet();
        size.decrementAndGet();

        return true;
      }

      return false;
    } finally {
      lock.writeLock().unlock();
    }
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    // grab the lock since returning true here means that the set contains
    // all the elements at one moment in time
    lock.readLock().lock();

    try {
      for (Object element : c) {
        if (!contains(element)) {
          return false;
        }
      }

      return true;
    } finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public boolean addAll(Collection<? extends Long> c) {
    boolean changed = false;

    //rely on add() holding the writeLock
    for (Long element : c) {
      if (add(element)) {
        changed = true;
      }
    }

    return changed;
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    boolean changed = false;

    // semantics of this method are that only elements in the collection
    // are present after completion; hold the lock so that this holds
    // for at least one moment

    lock.writeLock().lock();

    try {
      Iterator<Long> iterator = this.iterator();

      while (iterator.hasNext()) {
        if (!c.contains(iterator.next())) {
          // remove() here will update the version
          iterator.remove();
          size.decrementAndGet();
          changed = true;
        }
      }
    } finally {
      lock.writeLock().unlock();
    }

    return changed;
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    boolean changed = false;
    // simpler to reason about if we lock the set here
    lock.writeLock().lock();

    try {
      if (size() > c.size()) {
        for (Iterator<?> i = c.iterator(); i.hasNext(); ) {
          changed |= remove(i.next());
        }
      } else {
        for (Iterator<?> i = iterator(); i.hasNext(); ) {
          if (c.contains(i.next())) {
            i.remove();
            changed = true;
          }
        }
      }

      return changed;
    } finally {
      lock.writeLock().unlock();
    }
  }

  @Override
  public void clear() {
    lock.writeLock().lock();

    try {
      internalClear();
      version.incrementAndGet();
    } finally {
      lock.writeLock().unlock();
    }
  }

  /**
   * efficient deep-copy method
   *
   * @return deep copy of this set
   */
  @Override
  public SnapshotableSet<Long> makeSnapshot() {
    lock.readLock().lock();

    try {
      // create  new set
      LongHashSet copy = new LongHashSet(values.length, maxCapacity, hashFunction);

      // set the size and copy the values array
      copy.size.set(size.get());
      System.arraycopy(values, 0, copy.values, 0, values.length);

      return copy;
    } finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public SnapshotableSet<Long> makeTransientSnapshot() {
    return new SnapshotableSetImpl<Long>(
      Collections.<Long>synchronizedSet(new HashSet<Long>(this)),
      new SnapshotableSetImplFactory<Long>(new HashSetFactory<Long>())
    );
  }

  private void internalClear() {
    size.set(0);
    Arrays.fill(values, INITIAL_EMPTY);
  }

  @Override
  public synchronized boolean hasChanged() {
    long pastVersion = lastCheckedVersion;
    lastCheckedVersion = version.get();

    return (lastCheckedVersion != pastVersion);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Set)) {
      return false;
    }
    @SuppressWarnings("unchecked") Collection<Long> c = (Collection<Long>) o;
    if (c.size() != size()) {
      return false;
    }
    try {
      return containsAll(c);
    } catch (ClassCastException unused) {
      return false;
    } catch (NullPointerException unused) {
      return false;
    }
  }

  @Override
  public int hashCode() {
    int h = 0;
    Iterator<Long> i = iterator();

    while (i.hasNext()) {
      Long value = i.next();

      if (value != null) {
        h += value.hashCode();
      }
    }

    return h;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder((int) (size.get() * 8));
    Set<Long> copy = makeSnapshot();
    boolean first = true;

    sb.append("{");

    for (Long value : copy) {
      if (!first) {
        sb.append(", ");
      }

      sb.append(value.longValue());
      first = false;
    }

    sb.append("}");

    return sb.toString();
  }
}
