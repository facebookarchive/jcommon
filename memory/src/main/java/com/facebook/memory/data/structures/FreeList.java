package com.facebook.memory.data.structures;

import com.google.common.annotations.VisibleForTesting;

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

  /**
   * merges the range [offset, offset + size]
   * @param offset
   * @param size
   */
  void free(int offset, int size);

  /**
   * finds a free range of size 'size' if available
   * @param size
   * @return
   * @throws FailedAllocationException if no contiguous range exists >= size
   */
  int allocate(int size) throws FailedAllocationException;

  /**
   * used mostly for test cases;
   *
   * @return
   */
  @VisibleForTesting
  Set<IntRange> asRangeSet();

  /**
   * restores the rangeSet to [0, size - 1]
   * @param size
   */
  void reset(int size);
}
