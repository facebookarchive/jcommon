package com.facebook.memory.data.types;

import com.facebook.memory.data.types.definitions.OffHeapStructure;
import com.facebook.memory.data.types.definitions.PointerSlot;

public class LinkedListNode implements OffHeapStructure {
  private static final PointerSlot NEXT = new PointerSlot();
  private static final PointerSlot DATA_POINTER = new PointerSlot();

  protected final long address;

  public LinkedListNode(long address) {
    this.address = address;
  }

  @Override
  public long getAddress() {
    return address;
  }

  public long getNext() {
    return NEXT.accessor(address).get();
  }

  public LinkedListNode setNext(long next) {
    NEXT.accessor(address).put(next);

    return this;
  }

  public long getDataPointer() {
    return DATA_POINTER.accessor(address).get();
  }

  public LinkedListNode setDataPointer(long data) {
    DATA_POINTER.accessor(address).put(data);

    return this ;
  }
}
