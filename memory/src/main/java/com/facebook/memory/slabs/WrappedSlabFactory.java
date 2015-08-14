package com.facebook.memory.slabs;

public class WrappedSlabFactory implements SlabFactory {
  private final SlabFactory slabFactory;

  public WrappedSlabFactory(SlabFactory slabFactory) {
    this.slabFactory = slabFactory;
  }

  @Override
  public Slab create(int sizeBytes) {
    return slabFactory.create(sizeBytes);
  }
}
