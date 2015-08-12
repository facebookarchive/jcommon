package com.facebook.memory.data.structures;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.facebook.memory.FailedAllocationException;

public abstract class SimulatedOffHeapAccessor<T extends OffHeap>  implements DynamicOffHeapAccessor<T> {
  private static final AtomicLong NEXT_ADDRESS = new AtomicLong(1);

  private final Map<Long, T> data = Maps.newConcurrentMap();

  protected abstract T newItem(long address, int length);

  @Override
  public T create(int size) throws FailedAllocationException {
    long address = NEXT_ADDRESS.getAndIncrement();
    T t = newItem(address, size);

    data.put(address, t);

    return t;
  }

  @Override
  public T wrap(long address) {
    return data.get(address);
  }
}
