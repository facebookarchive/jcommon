package com.facebook.memory.slabs;

import com.facebook.memory.FailedAllocationException;

public interface Slab extends RawSlab {
  /**
   * trys to allocate the space. It may return fewer bytes (down to 0). Check the return structure for the amount
   * allocated
   *
   * @param sizeBytes
   * @return
   */
  Allocation tryAllocate(int sizeBytes);

  /**
   * allocates the requested space or fails
   * @param sizeBytes
   * @return
   * @throws FailedAllocationException
   */
  long allocate(int sizeBytes) throws FailedAllocationException;

  long getUsed();

  long getFree();

  void free(long address, int size);
}
