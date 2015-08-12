package com.facebook.memory.data.structures;

import com.google.common.base.MoreObjects;

import com.facebook.collections.bytearray.ByteArray;
import com.facebook.memory.FailedAllocationException;
import com.facebook.memory.data.types.definitions.IntSlot;
import com.facebook.memory.data.types.definitions.OffHeapStructure;
import com.facebook.memory.data.types.definitions.OffsetIntSlotAccessor;
import com.facebook.memory.data.types.definitions.PointerSlot;
import com.facebook.memory.slabs.Slab;
import com.facebook.memory.views.MemoryView;
import com.facebook.memory.views.MemoryViewFactory;

class LinkedListNode implements Annotatable, Annotated, OffHeapStructure {
  private static final PointerSlot ANNOTATION_ADDR = new PointerSlot();
  private static final PointerSlot NEXT_ADDR = new PointerSlot();
  private static final IntSlot KEY_SIZE = new IntSlot();
  private static final int TOTAL_HEADER_BYTES = 8 + 8 + 4 + 4;

  private final MemoryView memoryView;
  private final MemoryViewFactory memoryViewFactory;

  private LinkedListNode(MemoryView memoryView, MemoryViewFactory memoryViewFactory) {
    this.memoryView = memoryView;
    this.memoryViewFactory = memoryViewFactory;
  }

  /**
   * this wraps a given address
   * @param address
   * @param memoryViewFactory
   */
  LinkedListNode(long address, MemoryViewFactory memoryViewFactory) {
    this.memoryViewFactory = memoryViewFactory;
    int keySize = KEY_SIZE.accessor(address).get();
    OffsetIntSlotAccessor valueSizeAccessor = new OffsetIntSlotAccessor(address, getKeyBytesOffset() + keySize);
    int valueSize = valueSizeAccessor.get();
    
    memoryView = memoryViewFactory.wrap(address, keySize + valueSize + TOTAL_HEADER_BYTES);
  }

  public static LinkedListNode wrap(long address, MemoryViewFactory memoryViewFactory) {
    return new LinkedListNode(address, memoryViewFactory);
  }

  public static LinkedListNode create(ByteArray key, ByteArray value, MemoryViewFactory memoryViewFactory, Slab slab)
    throws FailedAllocationException {
    int keyLength = key.getLength();
    int valueLength = value.getLength();
    int size = keyLength + valueLength + TOTAL_HEADER_BYTES;
    long address = slab.allocate(size);
    MemoryView memoryView = memoryViewFactory.wrap(address, size);

    memoryView.putPointer(ANNOTATION_ADDR.getOffset(), 0L);
    memoryView.putInt(KEY_SIZE.getOffset(), keyLength);


    for (int i = 0; i < keyLength; i++) {
      memoryView.putByte(getKeyBytesOffset() + i, key.getAdjusted(i));
    }

    int valueSizeOffset = getKeyBytesOffset() + keyLength;
    int valueBytesOffset = valueSizeOffset + Integer.BYTES;

    memoryView.putInt(valueSizeOffset, valueLength);

    for (int i = 0; i < valueLength; i++) {
      memoryView.putByte(valueBytesOffset + i, value.getAdjusted(i));
    }

    return new LinkedListNode(memoryView, memoryViewFactory);
  }

  boolean keyEquals(ByteArray key) {
    return getKey().equals(key);
  }

  private static int getKeyBytesOffset() {
    return KEY_SIZE.getOffset() + Integer.BYTES;
  }

  private int getValueSizeOffset() {
    int keySize = KEY_SIZE.accessor(memoryView.getAddress()).get();

    return getKeyBytesOffset() + keySize;
  }

  private int getValueBytesOffset() {
    return getValueSizeOffset() + Integer.BYTES;
  }

  OffHeapByteArray getKey() {
    return OffHeapByteArrayImpl.wrap(memoryView.getAddress() + KEY_SIZE.getOffset());
  }

  OffHeapByteArray getValue() {
    return OffHeapByteArrayImpl.wrap(memoryView.getAddress() + getValueSizeOffset());
  }

  LinkedListNode next() {
    return new LinkedListNode(memoryView.getPointer(NEXT_ADDR.getOffset()), memoryViewFactory);
  }

  void setNext(long address) {
    memoryView.putPointer(NEXT_ADDR.getOffset(), address);
  }

  @Override
  public long getAddress() {
    return memoryView.getAddress();
  }

  int getSize() {
    return memoryView.getInt(KEY_SIZE.getOffset()) + memoryView.getInt(getValueSizeOffset()) +TOTAL_HEADER_BYTES;
  }

  @Override
  public void storeAnnotationAddress(OffHeap offheap) {
    memoryView.putPointer(ANNOTATION_ADDR.getOffset(), offheap.getAddress());
  }

  @Override
  public long getAnnotationAddress() {
    return memoryView.getPointer(ANNOTATION_ADDR.getOffset());
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .add("address", memoryView.getAddress())
      .add("size", getSize())
      .toString();
  }
}

