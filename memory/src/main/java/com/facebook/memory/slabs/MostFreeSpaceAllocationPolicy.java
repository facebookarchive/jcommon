package com.facebook.memory.slabs;

import java.util.Comparator;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.facebook.logging.Logger;
import com.facebook.logging.LoggerImpl;
import com.facebook.memory.FailedAllocationException;

public class MostFreeSpaceAllocationPolicy implements AllocationPolicy {
  private static final Logger LOGGER = LoggerImpl.getClassLogger();

  private final NavigableSet<Slab> slabSet;
  private final Set<Slab> batchedRemoveSet;
  private final ReadWriteLock removeLock = new ReentrantReadWriteLock();
  private volatile boolean started = false;

  public MostFreeSpaceAllocationPolicy(SlabPool slabPool) {
    Comparator<Slab> slabComparator = (o1, o2) -> Long.signum(o1.getFree() - o2.getFree());
    slabSet = new TreeSet<>(slabComparator);

    for (Slab slab : slabPool) {
      slabSet.add(slab);
    }

    batchedRemoveSet = new ConcurrentSkipListSet<>(slabComparator);
  }

  @Override
  public long allocate(SlabAllocationFunction slabAllocationFunction) throws FailedAllocationException {
    started = true;

    Slab slab;

    removeLock.writeLock().lock();

    try {
      if (!batchedRemoveSet.isEmpty()) {
        slabSet.removeAll(batchedRemoveSet);
        slabSet.addAll(batchedRemoveSet);
        batchedRemoveSet.clear();
      }

      slab = slabSet.pollLast();
    } finally {
      removeLock.writeLock().unlock();
    }

    try {
      long address = slabAllocationFunction.execute(slab);

      return address;
    } finally {
      synchronized (slabSet) {
        slabSet.add(slab);
      }
    }
  }

  @Override
  public void updateSlab(Slab slab) {
    if (started) {
      removeLock.readLock().lock();

      try {
        batchedRemoveSet.add(slab);
      } finally {
        removeLock.readLock().unlock();
      }
    }
  }
}
