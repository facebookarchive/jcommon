package com.facebook.collections.specialized;

import com.facebook.collections.SetFactory;

/**
 * Long in order to be compatible with SampledUniqueCount
 */
public class IntegerHashSetFactory implements SetFactory<Long, SnapshotableSet<Long>> {
  public static final int DEFAULT_MAX_SIZE = 8000;
  public static final int DEFAULT_INITIAL_SIZE = 4;

  private final int initialCapacity;
  private final int maxCapacity;

  public IntegerHashSetFactory(int initialCapacity, int maxCapacity) {
    this.initialCapacity = initialCapacity;
    this.maxCapacity = maxCapacity;
  }

  public IntegerHashSetFactory(int maxCapacity) {
    this(DEFAULT_INITIAL_SIZE, maxCapacity);
  }

  public IntegerHashSetFactory() {
    this(DEFAULT_INITIAL_SIZE, DEFAULT_MAX_SIZE);
  }

  public static IntegerHashSetFactory withInitialSize(int initialSize) {
    return new IntegerHashSetFactory(initialSize, DEFAULT_MAX_SIZE);
  }

  public static IntegerHashSetFactory withMaxSize(int maxSize) {
    return new IntegerHashSetFactory(DEFAULT_INITIAL_SIZE, maxSize);
  }

  @Override
  public SnapshotableSet<Long> create() {
    // TODO: the 2 *  should be smaller, maybe 1.2, but using 2 to avoid current bug in
    // SampledSetImpl that seem to exceed the max
    return new IntegerHashSet(initialCapacity, 2 * maxCapacity);
  }
}
