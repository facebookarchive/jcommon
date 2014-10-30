package com.facebook.memory.data.structures;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public abstract class SimulatedOffHeapAccessor<T extends OffHeap>  implements OffHeapAccessor<T> {
  private static final AtomicLong NEXT_ADDRESS = new AtomicLong(1);

  private final Map<Long, T> data = Maps.newConcurrentMap();

  protected abstract T newItem(long address);

  @Override
  public T create() {
    long address = NEXT_ADDRESS.getAndIncrement();
    T t = newItem(address);

    data.put(address, t);

    return t;
  }

  @Override
  public T wrap(long address) {
    return data.get(address);
  }
}
