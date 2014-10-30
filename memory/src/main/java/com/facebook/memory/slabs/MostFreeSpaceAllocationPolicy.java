package com.facebook.memory.slabs;

import java.util.Comparator;
import java.util.NavigableSet;
import java.util.TreeSet;

import com.facebook.logging.Logger;
import com.facebook.logging.LoggerImpl;
import com.facebook.memory.FailedAllocationException;

public class MostFreeSpaceAllocationPolicy implements AllocationPolicy {
  private static final Logger LOGGER = LoggerImpl.getClassLogger();

  private final NavigableSet<Slab> slabSet;

  public MostFreeSpaceAllocationPolicy(SlabPool slabPool) {
    slabSet = new TreeSet<>(new Comparator<Slab>() {
      @Override
      public int compare(Slab o1, Slab o2) {
        return Long.signum(o1.getFree() - o2.getFree());
      }
    });
    for (Slab slab : slabPool) {
      slabSet.add(slab);
    }
  }

  @Override
  public long allocate(SlabAllocationFunction slabAllocationFunction) throws FailedAllocationException {
    Slab slab = slabSet.pollLast();

    try {
      long address = slabAllocationFunction.execute(slab);

      return address;
    } finally {
      slabSet.add(slab);
    }
  }

  @Override
  public Slab getSlab(long sizeBytes) throws FailedAllocationException {
    Slab slab = slabSet.pollLast();

    return slab;
  }

  @Override
  public void updateSlab(Slab slab) {
    slabSet.add(slabSet.pollLast());
  }
}
