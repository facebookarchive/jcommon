package com.facebook.memory.slabs;

import com.facebook.memory.FailedAllocationException;

public interface Slab extends RawSlab {
  public static final int SLAB_INCREASE_FACTOR = 2;
  public static final long SLAB_MIN_INCREASE_SIZE = 2 * 1024 * 1024;

  public long allocate(long sizeBytes) throws FailedAllocationException;

  public long getUsed();

  public void free(long address, int size) throws FailedAllocationException;
}
