package com.facebook.memory.data.structures;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

import com.facebook.memory.FailedAllocationException;
import com.facebook.memory.data.types.definitions.OffHeapStructure;
import com.facebook.memory.data.types.definitions.PointerAccessor;
import com.facebook.memory.data.types.definitions.PointerSlot;
import com.facebook.memory.data.types.definitions.SizedOffHeapStructure;
import com.facebook.memory.data.types.definitions.Structs;
import com.facebook.memory.slabs.Slab;

class LinkedListBucketNode implements Annotatable, Annotated, OffHeapStructure {
  protected static final PointerSlot NEXT_ADDR = new PointerSlot();
  protected static final PointerSlot KEY_ADDRESS = new PointerSlot();
  protected static final PointerSlot VALUE_ADDRESS = new PointerSlot();
  protected static final int TOTAL_HEADER_BYTES = Structs.getStruct(LinkedListBucketNode.class).getStaticSlotsSize();

  protected final SizedOffHeapWrapper keyWrapper;
  protected final SizedOffHeapWrapper valueWrapper;
  protected final PointerAccessor nextAddrAccessor;
  protected final PointerAccessor keyAddressAccessor;
  protected final PointerAccessor valueAddressAccessor;

  protected LinkedListBucketNode(
    SizedOffHeapWrapper keyWrapper,
    SizedOffHeapWrapper valueWrapper,
    PointerAccessor nextAddrAccessor,
    PointerAccessor keyAddressAccessor,
    PointerAccessor valueAddressAccessor
  ) {
    this.keyWrapper = keyWrapper;
    this.valueWrapper = valueWrapper;
    this.nextAddrAccessor = nextAddrAccessor;
    this.keyAddressAccessor = keyAddressAccessor;
    this.valueAddressAccessor = valueAddressAccessor;
  }

  protected LinkedListBucketNode(long address, SizedOffHeapWrapper keyWrapper, SizedOffHeapWrapper valueWrapper) {
    this.keyWrapper = keyWrapper;
    this.valueWrapper = valueWrapper;
    nextAddrAccessor = NEXT_ADDR.accessor(address);
    keyAddressAccessor = KEY_ADDRESS.accessor(nextAddrAccessor);
    valueAddressAccessor = VALUE_ADDRESS.accessor(keyAddressAccessor);
  }

  public static LinkedListBucketNode wrap(
    long address,
    SizedOffHeapWrapper keyWrapper,
    SizedOffHeapWrapper valueWrapper
  ) {
    return new LinkedListBucketNode(address, keyWrapper, valueWrapper);
  }

  /**
   * allocates a new offheap node; sets the key and value addresses as well as initalizes pointers appropriately
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
    PointerAccessor keyAddressAccessor = KEY_ADDRESS.accessor(nextAccessor);
    PointerAccessor valueAddressAccessor = VALUE_ADDRESS.accessor(keyAddressAccessor);

    nextAccessor.put(0);
    keyAddressAccessor.put(key.getAddress());
    valueAddressAccessor.put(value.getAddress());

    return new LinkedListBucketNode(
      keyWrapper,
      valueWrapper,
      nextAccessor,
      keyAddressAccessor,
      valueAddressAccessor
    );
  }

  protected boolean keyEquals(SizedOffHeapStructure key) {
    return getKey().equals(key);
  }

  protected SizedOffHeapStructure getKey() {
    return keyWrapper.wrap(keyAddressAccessor.get());
  }

  protected SizedOffHeapStructure getValue() {
    return valueWrapper.wrap(valueAddressAccessor.get());
  }

  protected LinkedListBucketNode nextNode() {
    return new LinkedListBucketNode(nextAddrAccessor.get(), keyWrapper, valueWrapper);
  }

  protected void setNextAddress(long nextAddress) {
    nextAddrAccessor.put(nextAddress);
  }

  @Override
  public long getAddress() {
    return keyAddressAccessor.getBaseAddress();
  }

  protected int getSize() {
    return TOTAL_HEADER_BYTES;
  }

  protected int getPayLoadSize() {
    return getKey().getSize() + getValue().getSize();
  }

  protected int getTotalSize() {
    return getSize() + getPayLoadSize();
  }

  @Override
  public void storeAnnotationAddress(OffHeap offheap) {
    Preconditions.checkArgument(getAddress() == offheap.getAddress(), "should only be told that annotation is our address");
  }

  @Override
  public long getAnnotationAddress() {
    // the address of "this" is the item in cache that will be removed if eviction is necessary
    return getAddress();
  }

  public SizedOffHeapStructure replaceValue(SizedOffHeapStructure newValue) {
    SizedOffHeapStructure existingValue = getValue();

    valueAddressAccessor.put(newValue.getAddress());

    return existingValue;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .add("address", getAddress())
      .add("key", getKey())
      .add("value", getValue())
      .add("next", nextAddrAccessor.get())
      .add("size", getSize())
      .toString();
  }
}

