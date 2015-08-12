package com.facebook.memory.slabs;

import com.facebook.memory.FailedAllocationException;

/**
 * a function object to specify the allocation operation on a slab. In practice, this will often just be have
 *
 *   slab -> slab.allocate(size)
 *
 *   as its body. However, this object exists so that callers of functions using this may provide additional
 *   bookkeeping instructions to call when doing the allocation (see PolicyUpdatingAllocationFunciton and its usess)
 */
public interface SlabAllocationFunction {
  long allocateOn(Slab slab) throws FailedAllocationException;
}
