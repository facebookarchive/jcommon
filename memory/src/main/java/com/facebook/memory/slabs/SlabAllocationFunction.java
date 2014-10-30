package com.facebook.memory.slabs;

import com.facebook.collectionsbase.Function;
import com.facebook.memory.FailedAllocationException;

public interface SlabAllocationFunction extends Function<Slab, Long, FailedAllocationException> {
}
