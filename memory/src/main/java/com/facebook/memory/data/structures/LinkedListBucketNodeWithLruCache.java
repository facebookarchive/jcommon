package com.facebook.memory.data.structures;

import com.google.common.base.MoreObjects;

import com.facebook.memory.FailedAllocationException;
import com.facebook.memory.data.types.definitions.PointerAccessor;
import com.facebook.memory.data.types.definitions.PointerSlot;
import com.facebook.memory.data.types.definitions.SizedOffHeapStructure;
import com.facebook.memory.data.types.definitions.Structs;
import com.facebook.memory.slabs.Slab;

class LinkedListBucketNodeWithLruCache extends LinkedListBucketNode {
  static final PointerSlot CACHE_NEXT_ADDR = new PointerSlot();
  static final PointerSlot CACHE_PREVIOUS_ADDR = new PointerSlot();
  static final int TOTAL_HEADER_BYTES = Structs.getStruct(LinkedListBucketNodeWithLruCache.class).getStaticSlotsSize();

  private final PointerAccessor cacheNextAddressAccessor;
  private final PointerAccessor cachePreviousAddressAccessor;

  private LinkedListBucketNodeWithLruCache(
    SizedOffHeapWrapper keyWrapper,
    SizedOffHeapWrapper valueWrapper,
    PointerAccessor nextAddrAccessor,
    PointerAccessor keyAddressAccessor,
    PointerAccessor valueAddressAccessor,
    PointerAccessor cacheNextAddressAccessor,
    PointerAccessor cachePreviousAddressAccessor
  ) {
    super(keyWrapper, valueWrapper, nextAddrAccessor, keyAddressAccessor, valueAddressAccessor);
    this.cacheNextAddressAccessor = cacheNextAddressAccessor;
    this.cachePreviousAddressAccessor = cachePreviousAddressAccessor;
  }

  private LinkedListBucketNodeWithLruCache(
    long address,
    SizedOffHeapWrapper keyWrapper,
    SizedOffHeapWrapper valueWrapper
  ) {
    super(address, keyWrapper, valueWrapper);
    cacheNextAddressAccessor = CACHE_NEXT_ADDR.accessor(valueAddressAccessor);
    cachePreviousAddressAccessor = CACHE_PREVIOUS_ADDR.accessor(cacheNextAddressAccessor);
  }

  public static LinkedListBucketNodeWithLruCache wrap(
    long address,
    SizedOffHeapWrapper keyWrapper,
    SizedOffHeapWrapper valueWrapper
  ) {
    return new LinkedListBucketNodeWithLruCache(address, keyWrapper, valueWrapper);
  }

  /**
   * allocates a new offheap node; sets the key and value addresses as well as initalizes pointers appropriately
   * @param slab
   * @param key
   * @param value
   * @return
   * @throws FailedAllocationException
   */
  public static LinkedListBucketNodeWithLruCache create(
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
    PointerAccessor cacheNextAccessor = CACHE_NEXT_ADDR.accessor(valueAddressAccessor);
    PointerAccessor cachePreviousAccessor = CACHE_PREVIOUS_ADDR.accessor(cacheNextAccessor);

    nextAccessor.put(0);
    keyAddressAccessor.put(key.getAddress());
    valueAddressAccessor.put(value.getAddress());
    cacheNextAccessor.put(0);
    cachePreviousAccessor.put(0);

    return new LinkedListBucketNodeWithLruCache(
      keyWrapper,
      valueWrapper,
      nextAccessor,
      keyAddressAccessor,
      valueAddressAccessor,
      cacheNextAccessor,
      cachePreviousAccessor
      );
  }
  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .add("address", getAddress())
      .add("key", getKey())
      .add("value", getValue())
      .add("next", nextAddrAccessor.get())
      .add("cacheNext", cacheNextAddressAccessor.get())
      .add("cachePrevious", cachePreviousAddressAccessor.get())
      .add("size", getSize())
      .toString();
  }
}

