package com.facebook.memory;

import com.facebook.memory.slabs.Slab;
import com.facebook.memory.slabs.SlabFactory;
import com.facebook.memory.slabs.ThreadLocalSlab;

public class ThreadLocalSlabFactory implements SlabFactory {
  private final int threadLocalSlabSizeBytes;
  private final SlabFactory baseSlabFactory;

  public ThreadLocalSlabFactory(
    int threadLocalSlabSizeBytes,
    SlabFactory baseSlabFactory
  ) {
    this.threadLocalSlabSizeBytes = threadLocalSlabSizeBytes;
    this.baseSlabFactory = baseSlabFactory;
  }

  @Override
  public Slab create(int sizeBytes) {
    Slab baseSlab = baseSlabFactory.create(sizeBytes);
    return new ThreadLocalSlab(threadLocalSlabSizeBytes, baseSlab);
  }
}
