package com.facebook.memory.data.types.definitions;

import com.google.common.base.Preconditions;

import com.facebook.memory.FailedAllocationException;
import com.facebook.memory.slabs.Slab;
import com.facebook.memory.views.MemoryView;
import com.facebook.memory.views.MemoryView16;
import com.facebook.memory.views.MemoryView32;
import com.facebook.memory.views.MemoryView64;

public class Structs {
  public static Struct getStruct(Class<? extends OffHeapStructure> clazz) {
    Struct struct = Slot.STRUCT_MAP.get(clazz);

    if (struct == null) {
      Slot.forceSizing(clazz);
      struct = Slot.STRUCT_MAP.get(clazz);
    }

    Preconditions.checkArgument(struct != null, "unknown structure: " + clazz);

    return struct;
  }

  public static int getStructSize(Class<? extends OffHeapStructure> clazz) {
    Struct struct = getStruct(clazz);

    return struct.getOffset();
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
