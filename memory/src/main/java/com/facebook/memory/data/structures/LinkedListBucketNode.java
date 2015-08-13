package com.facebook.memory.data.structures;

import com.google.common.base.MoreObjects;

import com.facebook.collections.bytearray.ByteArray;
import com.facebook.memory.FailedAllocationException;
import com.facebook.memory.data.types.definitions.ByteArraySlot;
import com.facebook.memory.data.types.definitions.ByteArraySlotAccessor;
import com.facebook.memory.data.types.definitions.OffHeapStructure;
import com.facebook.memory.data.types.definitions.PointerAccessor;
import com.facebook.memory.data.types.definitions.PointerSlot;
import com.facebook.memory.data.types.definitions.Structs;
import com.facebook.memory.slabs.Slab;

class LinkedListBucketNode implements Annotatable, Annotated, OffHeapStructure {
  static final PointerSlot NEXT_ADDR = new PointerSlot();
  static final PointerSlot CACHE_NEXT_ADDR= new PointerSlot();
  static final PointerSlot CACHE_PREVIOUS_ADDR = new PointerSlot();
  static final ByteArraySlot KEY = new ByteArraySlot();
  static final ByteArraySlot VALUE = new ByteArraySlot();
  static final int TOTAL_HEADER_BYTES = Structs.getStruct(LinkedListBucketNode.class).getStaticFieldsSize();

  private final long address;


  private LinkedListBucketNode(long address) {
    this.address = address;
  }

  public static LinkedListBucketNode wrap(long address) {
    return new LinkedListBucketNode(address);
  }

  public static LinkedListBucketNode createEmpty(long address) {
    LinkedListBucketNode linkedListBucketNode = new LinkedListBucketNode(address);

    return linkedListBucketNode;
  }

  public static LinkedListBucketNode create(ByteArray key, ByteArray value, Slab slab)
    throws FailedAllocationException {
    int keyLength = key.getLength();
    int valueLength = value.getLength();
    int size = keyLength + valueLength + TOTAL_HEADER_BYTES;
    long address = slab.allocate(size);
    PointerAccessor nextAccessor = NEXT_ADDR.accessor(address);
    PointerAccessor cacheNextAccessor = CACHE_NEXT_ADDR.accessor(nextAccessor);
    PointerAccessor cachePreviousAccessor = CACHE_NEXT_ADDR.accessor(cacheNextAccessor);
    ByteArraySlotAccessor keyAccessor = KEY.create(cachePreviousAccessor, keyLength);
    ByteArraySlotAccessor valueAccessor = VALUE.create(keyAccessor, valueLength);

    nextAccessor.put(0);
    cacheNextAccessor.put(0);
    cachePreviousAccessor.put(0);


    for (int i = 0; i < keyLength; i++) {
      keyAccessor.put(i, key.getAdjusted(i));
    }
    // don't instantiate valueAccessor until the key size is stored

    for (int i = 0; i < valueLength; i++) {
      valueAccessor.put(i, value.getAdjusted(i));
    }

    return new LinkedListBucketNode(address);
  }

  boolean keyEquals(ByteArray key) {
    return getKey().equals(key);
  }

  OffHeapByteArray getKey() {
    return KEY.wrap(address).asByteArray();
  }

  OffHeapByteArray getValue() {
    return VALUE.wrap(address).asByteArray();

  }

  LinkedListBucketNode next() {
    return new LinkedListBucketNode(NEXT_ADDR.accessor(address).get());
  }

  void setNext(long nextAddress) {
    // TODO: can store this accessor for faster access if need be
    NEXT_ADDR.accessor(address).put(nextAddress);
  }

  @Override
  public long getAddress() {
    return address;
  }

  int getSize() {
    return TOTAL_HEADER_BYTES + KEY.wrap(address).getLength() + VALUE.wrap(address).getLength();
  }

  @Override
  public void storeAnnotationAddress(OffHeap offheap) {
    // no-op
  }


  @Override
  public long getAnnotationAddress() {
    // the address of "this" is the item in cache that will be removed if eviction is necessary
    return address;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .add("address", address)
      .add("key", KEY.wrap(address).asByteArray())
      .add("value", VALUE.wrap(address).asByteArray())
      .add("next", NEXT_ADDR.accessor(address).get())
      .add("cacheNext", CACHE_NEXT_ADDR.accessor(address).get())
      .add("cachePrevious", CACHE_PREVIOUS_ADDR.accessor(address).get())
      .add("size", getSize())
      .toString();
  }
}

