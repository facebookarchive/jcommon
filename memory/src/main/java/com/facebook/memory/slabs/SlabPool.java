package com.facebook.memory.slabs;

public interface SlabPool extends Iterable<Slab> {
  public Slab getSlabByAddress(long address);
  public Slab getSlabByIndex(int index);
  public void freeSlabPool();
  public int getSize();
}
