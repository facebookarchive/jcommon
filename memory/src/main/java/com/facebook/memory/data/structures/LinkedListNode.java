package com.facebook.memory.data.structures;

import com.facebook.memory.FailedAllocationException;
import com.facebook.memory.MemoryConstants;
import com.facebook.memory.slabs.Slab;
import com.facebook.memory.views.MemoryView;
import com.facebook.memory.views.MemoryViewFactory;

class LinkedListNode  {
  private final MemoryView memoryView;
  private final MemoryViewFactory memoryViewFactory;
  private static final int ANNOTATION_ADDR_OFFSET = 0;
  private static final int NEXT_ADDR_OFFSET = ANNOTATION_ADDR_OFFSET + MemoryConstants.ADDRESS_SIZE;
  private static final int KEY_SIZE_OFFSET = NEXT_ADDR_OFFSET + MemoryConstants.ADDRESS_SIZE;
  private static final int VALUE_SIZE_OFFSET = KEY_SIZE_OFFSET + Integer.BYTES;
  private static final int HEADER_SIZE = VALUE_SIZE_OFFSET + Integer.BYTES;

  private LinkedListNode(MemoryView memoryView, MemoryViewFactory memoryViewFactory) {
    this.memoryView = memoryView;
    this.memoryViewFactory = memoryViewFactory;
  }

  LinkedListNode(long address, MemoryViewFactory memoryViewFactory) {
    this.memoryViewFactory = memoryViewFactory;
    int keySize = memoryViewFactory.wrap(address + KEY_SIZE_OFFSET, Integer.BYTES).getInt();
    int valueSize = memoryViewFactory.wrap(address + VALUE_SIZE_OFFSET, Integer.BYTES).getInt();
    memoryView = memoryViewFactory.wrap(address, keySize + valueSize + HEADER_SIZE);
  }

  static LinkedListNode create(byte[] k, byte[] v, MemoryViewFactory memoryViewFactory, Slab slab)
    throws FailedAllocationException {
    int size = k.length + v.length + HEADER_SIZE;
    MemoryView memoryView = memoryViewFactory.wrap(slab.allocate(size), size);

    memoryView.putPointer(ANNOTATION_ADDR_OFFSET, 0L);
    memoryView.putInt(KEY_SIZE_OFFSET, k.length);
    memoryView.putInt(VALUE_SIZE_OFFSET, v.length);

    for (int i = 0; i < k.length; i++) {
      memoryView.putByte(HEADER_SIZE + i , k[i]);
    }

    for (int i = 0; i < v.length; i++) {
      memoryView.putByte(HEADER_SIZE + k.length + i, v[i]);
    }

    return new LinkedListNode(memoryView, memoryViewFactory);
  }

  boolean keyEquals(byte [] key) {
    if (key.length != memoryView.getInt(KEY_SIZE_OFFSET)) {
      return false;
    }

    for (int i = 0; i < key.length; i++) {
      if (key[i] != memoryView.getByte(i + HEADER_SIZE)) {
        return false;
      }
    }
    return true;
  }

  byte [] getValue() {
    int valueOffset = memoryView.getInt(KEY_SIZE_OFFSET) + HEADER_SIZE;
    byte [] result = new byte[memoryView.getInt(VALUE_SIZE_OFFSET)];
    for (int i = 0; i < memoryView.getInt(VALUE_SIZE_OFFSET); i++) {
      result[i] = memoryView.getByte(i + valueOffset);
    }
    return result;
  }

  LinkedListNode next() {
    return new LinkedListNode(memoryView.getLong(NEXT_ADDR_OFFSET), memoryViewFactory);
  }

  void setNext(long address) {
    memoryView.putPointer(NEXT_ADDR_OFFSET, address);
  }

  long getAddress() {
    return memoryView.getAddress();
  }

  int getSize() {
    return memoryView.getInt(KEY_SIZE_OFFSET) + memoryView.getInt(VALUE_SIZE_OFFSET) + HEADER_SIZE;
  }

  public void storeAnnotationAddress(OffHeap offheap) {
    memoryView.putPointer(ANNOTATION_ADDR_OFFSET, offheap.getAddress());
  }
}

