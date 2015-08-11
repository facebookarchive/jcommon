package com.facebook.memory.slabs;

public class ThreadLocalSlabFactory implements SlabFactory {
  private final int threadLocalSlabSizeBytes;
  private final SlabFactory baseSlabFactory;

  /**
   * @param threadLocalSlabSizeBytes - size of tlab
   * @param baseSlabFactory factory to create the backing slab
   */
  public ThreadLocalSlabFactory(
    int threadLocalSlabSizeBytes,
    SlabFactory baseSlabFactory
  ) {
    this.threadLocalSlabSizeBytes = threadLocalSlabSizeBytes;
    this.baseSlabFactory = baseSlabFactory;
  }

  /**
   * uses a ManagedSlabFactory to create the backing slab
   *
   * @param threadLocalSlabSizeBytes
   */
  public ThreadLocalSlabFactory( int threadLocalSlabSizeBytes) {
    this(threadLocalSlabSizeBytes, new ManagedSlabFactory());
  }

  @Override
  public Slab create(int sizeBytes) {
    Slab baseSlab = baseSlabFactory.create(sizeBytes);
    return new ThreadLocalSlab(threadLocalSlabSizeBytes, baseSlab);
  }
}
