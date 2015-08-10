package com.facebook.memory.data.structures;

import java.util.Set;

import com.facebook.memory.FailedAllocationException;

public interface FreeList {
  /**
   * increase the size of the range (adds to the RHS)
   * [start, end] -> [start, end+size]
   *
   * @param size
   */
  void extend(int size);
  void free(int offset, int size);
  int allocate(int size) throws FailedAllocationException;
  Set<IntRange> asRangeSet();
  void reset(int size);
}
