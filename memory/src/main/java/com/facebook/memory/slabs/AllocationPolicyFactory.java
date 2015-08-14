package com.facebook.memory.slabs;

public interface AllocationPolicyFactory {
  AllocationPolicy create(SlabPool slabPool);
}
