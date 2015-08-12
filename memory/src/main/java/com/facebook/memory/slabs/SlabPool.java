package com.facebook.memory.slabs;

public interface SlabPool extends Iterable<Slab> {
  Slab getSlabByAddress(long address);

  Slab getSlabByIndex(int index);

  void freeSlabPool();

  int getSize();
}
