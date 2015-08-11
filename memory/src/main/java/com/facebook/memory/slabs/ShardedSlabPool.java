package com.facebook.memory.slabs;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

import com.facebook.logging.Logger;
import com.facebook.logging.LoggerImpl;

public class ShardedSlabPool implements SlabPool {
  private static final Logger LOG = LoggerImpl.getClassLogger();

  private final RangeMap<Long, Slab> slabMap;
  private final List<Slab> slabList;

  public ShardedSlabPool(RangeMap<Long, Slab> slabMap) {
    this.slabMap = slabMap;
    slabList = new ArrayList<>(slabMap.asMapOfRanges().values());
  }

  public static ShardedSlabPool create(SlabFactory slabFactory, int numSlabs, int slabSizeBytes) {
    RangeMap<Long, Slab> slabMap = TreeRangeMap.create();

    for (int i = 0; i < numSlabs; i++) {
      Slab slab = slabFactory.create(slabSizeBytes);
      long baseAddress = slab.getBaseAddress();
      long endAddress = baseAddress + slab.getSize();
      Range<Long> range = Range.closedOpen(baseAddress, endAddress);

      slabMap.put(range, slab);
    }

    return new ShardedSlabPool(slabMap);
  }

  @Override
  public Slab getSlabByAddress(long address) {
    Slab slab = slabMap.get(address);

    return Preconditions.checkNotNull(slab);
  }

  @Override
  public Slab getSlabByIndex(int index) {
    return slabList.get(index);
  }

  @VisibleForTesting
  RangeMap<Long, Slab> getSlabMap() {
    return slabMap;
  }

  @Override
  public void freeSlabPool() {
    for (Slab slab : slabList) {
      try {
        slab.freeSlab();
      } catch (Exception e) {
        LOG.error(e, "error freeing slab %s", slab);
      }
    }
  }

  @Override
  public int getSize() {
    return slabList.size();
  }


  @Override
  public Iterator<Slab> iterator() {
    return slabList.listIterator();
  }

  @Override
  public void forEach(Consumer<? super Slab> action) {
    slabList.forEach(action);
  }

  @Override
  public Spliterator<Slab> spliterator() {
    return slabList.spliterator();
  }
}


