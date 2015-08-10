package com.facebook.memory.slabs;

import com.google.common.base.Preconditions;
import sun.misc.Unsafe;

import com.facebook.memory.UnsafeAccessor;
import com.facebook.memory.views.ByteStream;
import com.facebook.memory.views.BytesVisitor;
import com.facebook.memory.views.MemoryView;
import com.facebook.memory.views.MemoryView32;

public class OffHeapSlab implements RawSlab {
  private final Unsafe unsafe = UnsafeAccessor.get();
  private final long size;
  private final long baseAddress;
  private volatile boolean isValid = true;

  public OffHeapSlab(long size) {
    this.size = size;
    baseAddress = unsafe.allocateMemory(size);
  }

  public static Factory factory() {
    return new Factory();
  }

  @Override
  public long getBaseAddress() {
    return baseAddress;
  }

  @Override
  public byte getByte(long address) {
    checkIsValid();

    return unsafe.getByte(address);
  }

  @Override
  public MemoryView get(long address, int sizeBytes) {
    checkIsValid();

    MemoryView memoryView = MemoryView32.factory()
      .wrap(address, sizeBytes);

    return memoryView;
  }

  @Override
  public void visitBytes(long address, long sizeBytes, BytesVisitor bytesVisitor) {
    checkIsValid();
    Preconditions.checkArgument(address + sizeBytes < baseAddress + size);

    for (long i = address; i < sizeBytes; i++) {
      bytesVisitor.visit(i, this.getByte(i));
    }

  }

  @Override
  public void putByte(long address, byte b) {
    checkIsValid();

    unsafe.putByte(address, b);
  }

  @Override
  public void put(long address, ByteStream byteStream) {
    while (byteStream.hasNext()) {
      unsafe.putByte(address, byteStream.nextByte());
      address++;
    }
  }

  @Override
  public long getSize() {
    checkIsValid();
    return size;
  }

  @Override
  public void freeSlab() {
    checkIsValid();
    unsafe.freeMemory(baseAddress);
    isValid = false;
  }

  @Override
  public long expand(long sizeBytes) {
    Preconditions.checkArgument(sizeBytes > this.size);

    unsafe.reallocateMemory(baseAddress, sizeBytes);

    return sizeBytes;
  }

  @Override
  public String toString() {
    return "OffHeapSlab{" +
      "size=" + size +
      ", baseAddress=" + baseAddress +
      ", isValid=" + isValid +
      '}';
  }

  private void checkIsValid() {
    if (!isValid) {
      throw new IllegalStateException("invalid slab, memory has been freed");
    }
  }

  public static class Factory {
    public RawSlab create(long size) {
      return new OffHeapSlab(size);
    }
  }
}
