package com.facebook.memory.data.types.definitions;

import com.facebook.memory.views.MemoryView;
import com.facebook.memory.views.MemoryView32;

public class OffHeapAccessor {
  private final MemoryView memoryView;

  public OffHeapAccessor(long address, Class<? extends OffHeapStructure> clazz) {
    memoryView = MemoryView32.factory().wrap(address, Structs.getStructSize(clazz));
  }

  public MemoryView getMemoryView() {
    return memoryView;
  }
}
