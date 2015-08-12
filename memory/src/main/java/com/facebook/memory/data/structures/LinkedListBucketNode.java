package com.facebook.memory.data.structures;

import com.google.common.base.MoreObjects;

import com.facebook.memory.FailedAllocationException;
import com.facebook.memory.data.types.definitions.OffHeapStructure;
import com.facebook.memory.data.types.definitions.PointerAccessor;
import com.facebook.memory.data.types.definitions.PointerSlot;
import com.facebook.memory.data.types.definitions.SizedOffHeapStructure;
import com.facebook.memory.data.types.definitions.Structs;
import com.facebook.memory.slabs.Slab;

class LinkedListBucketNode implements Annotatable, Annotated, OffHeapStructure {
  static final PointerSlot NEXT_ADDR = new PointerSlot();
  static final PointerSlot CACHE_NEXT_ADDR = new PointerSlot();
  static final PointerSlot CACHE_PREVIOUS_ADDR = new PointerSlot();
  static final PointerSlot KEY_ADDRESS = new PointerSlot();
  static final PointerSlot VALUE_ADDRESS = new PointerSlot();
  static final int TOTAL_HEADER_BYTES = Structs.getStruct(LinkedListBucketNode.class).getStaticSlotsSize();

  private final long address;
  private final SizedOffHeapWrapper keyWrapper;
  private final SizedOffHeapWrapper valueWrapper;

  private LinkedListBucketNode(long address, SizedOffHeapWrapper keyWrapper, SizedOffHeapWrapper valueWrapper) {
    this.address = address;
    this.keyWrapper = keyWrapper;
    this.valueWrapper = valueWrapper;
  }

  public static LinkedListBucketNode wrap(
    long address,
    SizedOffHeapWrapper keyWrapper,
    SizedOffHeapWrapper valueWrapper
  ) {
    return new LinkedListBucketNode(address, keyWrapper, valueWrapper);
  }

  public static LinkedListBucketNode createEmpty(
    long address,
    SizedOffHeapWrapper keyWrapper,
    SizedOffHeapWrapper valueWrapper
  ) {
    LinkedListBucketNode linkedListBucketNode = new LinkedListBucketNode(address, keyWrapper, valueWrapper);

    return linkedListBucketNode;
  }

  /**
   * @param slab
   * @param key
   * @param value
   * @return
   * @throws FailedAllocationException
   */
  public static LinkedListBucketNode create(
    Slab slab,
    SizedOffHeapStructure key,
    SizedOffHeapStructure value,
    SizedOffHeapWrapper keyWrapper,
    SizedOffHeapWrapper valueWrapper
  ) throws FailedAllocationException {
    long address = slab.allocate(TOTAL_HEADER_BYTES);
    PointerAccessor nextAccessor = NEXT_ADDR.accessor(address);
    PointerAccessor cacheNextAccessor = CACHE_NEXT_ADDR.accessor(nextAccessor);
    PointerAccessor cachePreviousAccessor = CACHE_PREVIOUS_ADDR.accessor(cacheNextAccessor);
    PointerAccessor keyAddressAccessor = KEY_ADDRESS.accessor(cachePreviousAccessor);
    PointerAccessor valueAddressAccessor = VALUE_ADDRESS.accessor(keyAddressAccessor);

    nextAccessor.put(0);
    cacheNextAccessor.put(0);
    cachePreviousAccessor.put(0);
    keyAddressAccessor.put(key.getAddress());
    valueAddressAccessor.put(value.getAddress());

    return new LinkedListBucketNode(address, keyWrapper, valueWrapper);
  }

  boolean keyEquals(SizedOffHeapStructure key) {
    return getKey().equals(key);
  }

  SizedOffHeapStructure getKey() {
    return keyWrapper.wrap(KEY_ADDRESS.accessor(address).get());
  }

  SizedOffHeapStructure getValue() {
    return valueWrapper.wrap(VALUE_ADDRESS.accessor(address).get());
  }

  LinkedListBucketNode next() {
    return new LinkedListBucketNode(NEXT_ADDR.accessor(address).get(), keyWrapper, valueWrapper);
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
    return TOTAL_HEADER_BYTES;
  }

  int getPayLoadSize() {
    return getKey().getSize() + getValue().getSize();
  }

  int getTotalSize() {
    return getSize() + getPayLoadSize();
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

  public SizedOffHeapStructure replaceValue(SizedOffHeapStructure newValue) {
    SizedOffHeapStructure existingValue = getValue();

    VALUE_ADDRESS.accessor(address).put(newValue.getAddress());

    return existingValue;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .add("address", address)
      .add("key", getKey())
      .add("value", getValue())
      .add("next", NEXT_ADDR.accessor(address).get())
      .add("cacheNext", CACHE_NEXT_ADDR.accessor(address).get())
      .add("cachePrevious", CACHE_PREVIOUS_ADDR.accessor(address).get())
      .add("size", getSize())
      .toString();
  }
}

