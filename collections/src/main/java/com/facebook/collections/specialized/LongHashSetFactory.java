package com.facebook.collections.specialized;

import com.facebook.collections.SetFactory;

public class LongHashSetFactory implements SetFactory<Long, SnapshotableSet<Long>> {
  public static final int DEFAULT_INITIAL_SIZE = 4;
  public static final int DEFAULT_MAX_SIZE = 8000;

  private final int initialSize;
  private final int maxSetSize;

  public LongHashSetFactory(int initialSize, int maxSetSize) {
    this.initialSize = initialSize;
    this.maxSetSize = maxSetSize;
  }

  public LongHashSetFactory(int maxSetSize) {
    this(DEFAULT_INITIAL_SIZE, maxSetSize);
  }

  public static LongHashSetFactory withInitialSize(int initialSize) {
    return new LongHashSetFactory(initialSize, DEFAULT_MAX_SIZE);
  }

  public static LongHashSetFactory withMaxSize(int maxSize) {
    return new LongHashSetFactory(DEFAULT_INITIAL_SIZE, maxSize);
  }

  @Override
  public SnapshotableSet<Long> create() {
    // we don't want more than 20% of maxSetSize allocated
    return new LongHashSet(initialSize, (int)((1.2f)*maxSetSize));
  }
}
