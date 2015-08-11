package com.facebook.memory.slabs;

public interface SlabManager {
  public Slab allocateSlab(long size);

  /**
   *
   * @param slab - source slab
   * @param newSize - size
   * @return
   */
  public Slab expandSlab(Slab slab, long newSize);

  public Slab freeSlab(Slab slab);
}
