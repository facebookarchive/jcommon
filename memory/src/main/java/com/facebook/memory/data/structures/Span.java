package com.facebook.memory.data.structures;

import sun.misc.Unsafe;

import com.facebook.memory.UnsafeAccessor;

public class Span {
  private static final Unsafe UNSAFE = UnsafeAccessor.get();

  private final long dataAddress;
  private final int dataSize;

  public Span(long dataAddress, int dataSize) {
    this.dataAddress = dataAddress;
    this.dataSize = dataSize;
  }

  public static Span from(long address) {
    long dataAddress = UNSAFE.getAddress(address);
    int dataSize = UNSAFE.getInt(address + UNSAFE.addressSize());

    return new Span(dataAddress, dataSize);
  }

  public long getDataAddress() {
    return dataAddress;
  }

  public int getDataSize() {
    return dataSize;
  }

  public void writeTo(long address) {
    UNSAFE.putAddress(address, dataAddress);
    UNSAFE.putInt(address + UNSAFE.addressSize(), dataSize);
  }

  public static int size() {
    return UNSAFE.addressSize() + Integer.BYTES;
  }
}
