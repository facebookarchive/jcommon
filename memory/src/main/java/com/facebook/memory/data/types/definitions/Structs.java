package com.facebook.memory.data.types.definitions;

import com.google.common.base.Preconditions;

public class Structs {
  public static synchronized Struct getStruct(Class<? extends OffHeapStructure> clazz) {
    Struct struct = Slot.STRUCT_MAP.get(clazz);

    if (struct == null) {
      Slot.forceSizing(clazz);
      struct = Slot.STRUCT_MAP.get(clazz);
    }

    Preconditions.checkArgument(struct != null, "unknown structure: " + clazz);

    return struct;
  }

  public static int getStaticFieldSize(Class<? extends OffHeapStructure> clazz) {
    Struct struct = getStruct(clazz);

    return struct == null ? 0 : struct.getStaticSlotsSize();
  }
}
