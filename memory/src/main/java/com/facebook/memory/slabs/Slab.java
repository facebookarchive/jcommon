package com.facebook.memory.slabs;

import com.facebook.memory.FailedAllocationException;

public interface Slab extends RawSlab {
  int SLAB_INCREASE_FACTOR = 2;

  long SLAB_MIN_INCREASE_SIZE = 2 * 1024 * 1024;

  long allocate(long sizeBytes) throws FailedAllocationException;

  long getUsed();

  long getFree();

  void free(long address, int size);
}
