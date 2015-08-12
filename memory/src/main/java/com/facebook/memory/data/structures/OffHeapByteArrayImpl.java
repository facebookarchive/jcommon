package com.facebook.memory.data.structures;

import com.google.common.base.Preconditions;

import java.util.Arrays;

import com.facebook.collections.bytearray.AbstractByteArray;
import com.facebook.memory.FailedAllocationException;
import com.facebook.memory.MemoryConstants;
import com.facebook.memory.data.types.definitions.ByteArraySlot;
import com.facebook.memory.data.types.definitions.IntSlot;
import com.facebook.memory.slabs.Slab;

public class OffHeapByteArrayImpl extends AbstractByteArray implements OffHeapByteArray {
  private static final IntSlot LENGTH = new IntSlot();
  private static final ByteArraySlot DATA_OFFSET = new ByteArraySlot();

  private final long address;
  private final int length;

  /**
   * wrap a newly allocated OffHeapByteArray; will store lenght in the appropriate memory location
   *
   * @param address
   * @param length
   */
  private OffHeapByteArrayImpl(long address, int length) {
    this.address = address;
    this.length = length;
  }

  private OffHeapByteArrayImpl(long address) {
    this(address, LENGTH.accessor(address).get());
  }

  public static OffHeapByteArray allocate(int length, Slab slab) throws FailedAllocationException {
    long address = slab.allocate(length);
    LENGTH.accessor(address).put(length);

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
    OffHeapByteArray offHeapByteArray = new OffHeapByteArrayImpl(slab.allocate(bytes.length), bytes.length);
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
    return DATA_OFFSET.accessor(address).get(pos);
  }

  @Override
  public void putAdjusted(int pos, byte b) {
    Preconditions.checkArgument(pos >= 0 && pos < length);
    DATA_OFFSET.accessor(address).put(pos, b);
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
  public String toString() {
    return "OffHeapByteArray{" +
      "address=" + address +
      ", length=" + length +
      ", array=" + arrayToString() +
      "}";
  }

  // expensive
  private String arrayToString() {
    byte[] buf = new byte[length];
    int i = 0;

    for (byte b : this) {
      buf[i++] = b;
    }

    return Arrays.toString(buf);
  }
}
