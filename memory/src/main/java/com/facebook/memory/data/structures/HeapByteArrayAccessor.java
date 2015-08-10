package com.facebook.memory.data.structures;

public abstract class HeapByteArrayAccessor extends SimulatedOffHeapAccessor<AnnotatedByteArray>
  implements ByteArrayAccessor {
  @Override
  protected AnnotatedByteArray newItem(long address) {
    return new AnnotatedByteArray(address, address);
  }
}
