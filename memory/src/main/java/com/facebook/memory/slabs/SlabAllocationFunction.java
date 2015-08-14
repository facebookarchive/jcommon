package com.facebook.memory.slabs;

import com.facebook.memory.FailedAllocationException;

public interface SlabAllocationFunction {
  long allocateOn(Slab slab) throws FailedAllocationException;
}
