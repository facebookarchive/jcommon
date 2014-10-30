package com.facebook.memory.slabs;

import com.facebook.memory.slabs.DualAllocationPolicy;
import com.facebook.memory.slabs.MostFreeSpaceAllocationPolicy;
import com.facebook.memory.slabs.SlabPool;
import com.facebook.memory.slabs.ThreadLocalAllocationPolicy;

public class ThreadLocalThenMostFreePolicy extends DualAllocationPolicy {
  public ThreadLocalThenMostFreePolicy(SlabPool slabPool) {
    super(new ThreadLocalAllocationPolicy(slabPool), new MostFreeSpaceAllocationPolicy(slabPool));
  }
}
