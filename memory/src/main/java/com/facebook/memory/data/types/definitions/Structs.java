package com.facebook.memory.data.types.definitions;

import com.google.common.base.Preconditions;

import java.util.concurrent.atomic.AtomicInteger;

import com.facebook.memory.FailedAllocationException;
import com.facebook.memory.slabs.Slab;
import com.facebook.memory.views.MemoryView;
import com.facebook.memory.views.MemoryView16;
import com.facebook.memory.views.MemoryView32;
import com.facebook.memory.views.MemoryView64;

public class Structs {
  public static int getStructSize(Class<? extends OffHeapStructure> clazz) {
    AtomicInteger size = Slot.OFFSETS_BY_STRUCTURE.get(clazz);

    if (size == null) {
      Slot.forceSizing(clazz);
      size = Slot.OFFSETS_BY_STRUCTURE.get(clazz);
    }

    Preconditions.checkArgument(size != null, "unknown structure: " + clazz);

    return size.get();
  }

  public static MemoryView allocateMemoryView(Class<? extends OffHeapStructure> clazz, Slab slab)
    throws FailedAllocationException {
    int structSize = getStructSize(clazz);
    long address = allocate(clazz, slab);
    MemoryView memoryView;

    if (structSize < Short.MAX_VALUE) {
      memoryView = MemoryView16.factory().wrap(address, structSize);
    } else if (structSize < Integer.MAX_VALUE) {
      memoryView = MemoryView32.factory().wrap(address, structSize);
    } else {
      memoryView = MemoryView64.factory().wrap(address, structSize);
    }

    return memoryView;
  }

  public static long allocate(Class<? extends OffHeapStructure> clazz, Slab slab) throws FailedAllocationException {
    int structSize = getStructSize(clazz);
    long address = slab.allocate(structSize);

    return address;
  }
}
