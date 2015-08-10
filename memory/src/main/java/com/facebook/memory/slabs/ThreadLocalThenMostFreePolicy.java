package com.facebook.memory.slabs;

public class ThreadLocalThenMostFreePolicy extends DualAllocationPolicy {
  public ThreadLocalThenMostFreePolicy(SlabPool slabPool) {
    super(new ThreadLocalAllocationPolicy(slabPool), new MostFreeSpaceAllocationPolicy(slabPool));
  }
}
