package com.facebook.memory.data.structures;

import java.util.Set;

import com.facebook.memory.FailedAllocationException;

public interface FreeList {
  public void extend(int size);
  public void free(int offset, int size);
  public long allocate(int size) throws FailedAllocationException;
  public Set<IntRange> asRangeSet();
  public void reset(int size);
}
