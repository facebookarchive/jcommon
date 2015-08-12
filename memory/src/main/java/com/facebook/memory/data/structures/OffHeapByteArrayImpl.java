package com.facebook.memory.data.structures;

import com.google.common.base.Preconditions;

import com.facebook.collections.bytearray.AbstractByteArray;
import com.facebook.memory.FailedAllocationException;
import com.facebook.memory.MemoryConstants;
import com.facebook.memory.data.types.definitions.ByteArraySlot;
import com.facebook.memory.slabs.Slab;

/**
 * the layout is
 *
 * @address = {length, data}
 */
public class OffHeapByteArrayImpl extends AbstractByteArray implements OffHeapByteArray {
  private static final ByteArraySlot BYTE_ARRAY = new ByteArraySlot();

  private final long address;
  private final int length;

  /**
   * wrap a newly allocated OffHeapByteArray; will store length in the appropriate memory location
   *
   * @param address
   * @param length
   */
  protected OffHeapByteArrayImpl(long address, int length) {
    this.address = address;
    this.length = length;
  }

  protected OffHeapByteArrayImpl(long address) {
    this(address, BYTE_ARRAY.wrap(address).getLength());
  }

  public static OffHeapByteArray allocate(int length, Slab slab) throws FailedAllocationException {
    long address = slab.allocate(length + Integer.BYTES);
    BYTE_ARRAY.create(address, length);

    return new OffHeapByteArrayImpl(address, length);
  }

  /**
   * reads the length from the off-heap location
   *
   * @param address
   * @return OffHeapByteArray at address
   */
  public static OffHeapByteArray wrap(long address) {
    return new OffHeapByteArrayImpl(address);
  }

  public static OffHeapByteArray fromHeapByteArray(byte[] bytes, Slab slab) throws FailedAllocationException {
    int sizeBytes = Integer.BYTES + bytes.length;
    OffHeapByteArray offHeapByteArray = new OffHeapByteArrayImpl(
      slab.allocate(sizeBytes),
      bytes.length
    );

    BYTE_ARRAY.wrap(offHeapByteArray.getAddress()).putLength(bytes.length);

    int i = 0;

    for (byte b : bytes) {
      offHeapByteArray.putAdjusted(i++, b);
    }

    return offHeapByteArray;
  }

  @Override
  public int getLength() {
    return length;
  }

  @Override
  public byte getAdjusted(int pos) {
    Preconditions.checkArgument(pos >= 0 && pos < length);
    return BYTE_ARRAY.wrap(address).get(pos);
  }

  @Override
  public void putAdjusted(int pos, byte b) {
    Preconditions.checkArgument(pos >= 0 && pos < length);
    BYTE_ARRAY.wrap(address).put(pos, b);
  }

  @Override
  public boolean isNull() {
    return address == MemoryConstants.NO_ADDRESS;
  }

  @Override
  public long getAddress() {
    return address;
  }

  @Override
  public int getSize() {
    return Integer.BYTES + getLength();
  }

  @Override
  public String toString() {
    return "OffHeapByteArray{" +
      "address=" + address +
      ", length=" + length +
      ", array=" + arrayToString() +
      "}";
  }
}
