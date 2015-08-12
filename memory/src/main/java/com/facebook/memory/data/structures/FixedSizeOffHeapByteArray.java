package com.facebook.memory.data.structures;

import com.google.common.base.Preconditions;
import sun.misc.Unsafe;

import com.facebook.collections.bytearray.AbstractByteArray;
import com.facebook.memory.FailedAllocationException;
import com.facebook.memory.MemoryConstants;
import com.facebook.memory.UnsafeAccessor;
import com.facebook.memory.slabs.Slab;

public class FixedSizeOffHeapByteArray extends AbstractByteArray implements OffHeapByteArray {
  private static final Unsafe unsafe = UnsafeAccessor.get();

  private final long address;
  private final int length;

  /**
   * wrap a newly allocated OffHeapByteArray; will store length in the appropriate memory location
   *
   * @param address
   * @param length
   */
  private FixedSizeOffHeapByteArray(long address, int length) {
    this.address = address;
    this.length = length;
  }

  public static FixedSizeOffHeapByteArray allocate(int length, Slab slab) throws FailedAllocationException {
    long address = slab.allocate(length);

    return new FixedSizeOffHeapByteArray(address, length);
  }

  /**
   * reads the length from the off-heap location
   *
   * @param address
   * @return OffHeapByteArray at address
   */
  public static FixedSizeOffHeapByteArray wrap(long address, int length) {
    return new FixedSizeOffHeapByteArray(address, length);
  }

  public static FixedSizeOffHeapByteArray fromHeapByteArray(byte[] bytes, Slab slab) throws FailedAllocationException {
    FixedSizeOffHeapByteArray offHeapByteArray = new FixedSizeOffHeapByteArray(
      slab.allocate(bytes.length), bytes.length
    );
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
  public int getSize() {
    return length;
  }

  @Override
  public byte getAdjusted(int pos) {
    Preconditions.checkArgument(pos >= 0 && pos < length);
    return unsafe.getByte(address + pos);
  }

  @Override
  public void putAdjusted(int pos, byte b) {
    Preconditions.checkArgument(pos >= 0 && pos < length);
    unsafe.putByte(address + pos, b);
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
    return "FixedSizeOffHeapByteArray{" +
      "address=" + address +
      ", length=" + length +
      ", array=" + arrayToString() +
      "}";
  }
}
