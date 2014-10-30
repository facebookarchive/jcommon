package com.facebook.memory.data.types;

import com.facebook.memory.data.types.definitions.OffHeapStructure;
import com.facebook.memory.data.types.definitions.PointerSlot;

public class DoublyLinkedListNode extends LinkedListNode implements OffHeapStructure {
  private static final PointerSlot PREVIOUS = new PointerSlot();

  public DoublyLinkedListNode(long address) {
    super(address);
  }

  public long getPrevious() {
    return PREVIOUS.accessor(address).get();
  }

  public DoublyLinkedListNode setPrevious(long value) {
    PREVIOUS.accessor(address).put(value);

    return this;
  }

  @Override
  public LinkedListNode setNext(long value) {
    super.setNext(value);

    return this;
  }

  @Override
  public DoublyLinkedListNode setDataPointer(long value) {
    super.setDataPointer(value);

    return this;
  }
}
