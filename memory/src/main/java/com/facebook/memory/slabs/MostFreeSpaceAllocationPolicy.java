package com.facebook.memory.slabs;

import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.facebook.logging.Logger;
import com.facebook.logging.LoggerImpl;
import com.facebook.memory.FailedAllocationException;

/**
 * this class keeps an *approximately ordered* heap of slabs by free space. Concurrency can cause this not to be
 * true
 */
public class MostFreeSpaceAllocationPolicy implements AllocationPolicy {
  private static final Logger LOGGER = LoggerImpl.getClassLogger();

  private final PriorityBlockingQueue<Slab> slabQueue;
  private final Set<Slab> batchedRemoveSet;
  private final ReadWriteLock removeLock = new ReentrantReadWriteLock();
  private volatile boolean started = false;

  public MostFreeSpaceAllocationPolicy(SlabPool slabPool) {
    // we want descending order
    Comparator<Slab> slabComparator = (o1, o2) -> Long.signum(o2.getFree() - o1.getFree());

    slabQueue = new PriorityBlockingQueue(slabPool.getSize(), slabComparator);

    for (Slab slab : slabPool) {
      slabQueue.add(slab);
    }

    batchedRemoveSet = new ConcurrentSkipListSet<>(slabComparator);
  }

  @Override
  public Allocation tryAllocate(SlabTryAllocationFunction tryAllocationFunction) {
    Slab slab = getSlabForAllocation();

    try {
      Allocation allocation = tryAllocationFunction.tryAllocateOn(slab);

      return allocation;
    } finally {
      slabQueue.add(slab);
    }
  }

  @Override
  public long allocate(SlabAllocationFunction allocationFunction) throws FailedAllocationException {
    Slab slab = getSlabForAllocation();

    try {
      long address = allocationFunction.allocateOn(slab);

      return address;
    } finally {
      slabQueue.add(slab);
    }
  }

  @Override
  public void updateSlab(Slab slab) {
    if (started) {
      scheduleSlab(slab);
    }
  }

  /**
   * this indicates a slab's free space has changed and needs to be updated
   * @param slab
   */
  private void scheduleSlab(Slab slab) {
    removeLock.readLock().lock();

    try {
      batchedRemoveSet.add(slab);
    } finally {
      removeLock.readLock().unlock();
    }
  }

  private Slab getSlabForAllocation() {
    started = true;

    processSlabUpdates();

    Slab slab;

    removeLock.readLock().lock();

    try {
      slab = slabQueue.take();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } finally {
      removeLock.readLock().unlock();
    }

    return slab;
  }

  private void processSlabUpdates() {
    if (!batchedRemoveSet.isEmpty()) {
      removeLock.writeLock().lock();

      try {
        if (!batchedRemoveSet.isEmpty()) {
          slabQueue.removeIf(batchedRemoveSet::contains);
          slabQueue.addAll(batchedRemoveSet);
          batchedRemoveSet.clear();
        }
      } finally {
        removeLock.writeLock().unlock();
      }
    }
  }
}
