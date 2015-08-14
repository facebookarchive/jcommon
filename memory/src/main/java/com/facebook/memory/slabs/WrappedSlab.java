package com.facebook.memory.slabs;

import com.facebook.memory.FailedAllocationException;
import com.facebook.memory.views.ByteStream;
import com.facebook.memory.views.BytesVisitor;
import com.facebook.memory.views.MemoryView;

public class WrappedSlab implements Slab {
  private final Slab slab;

  public WrappedSlab(Slab slab) {
    this.slab = slab;
  }

  @Override
  public Allocation tryAllocate(int sizeBytes) {
    return slab.tryAllocate(sizeBytes);
  }

  @Override
  public long allocate(int sizeBytes) throws FailedAllocationException {
    return slab.allocate(sizeBytes);
  }

  @Override
  public long getUsed() {
    return slab.getUsed();
  }

  @Override
  public long getFree() {
    return slab.getFree();
  }

  @Override
  public void free(long address, int size) {
    slab.free(address, size);
  }

  @Override
  public long getBaseAddress() {
    return slab.getBaseAddress();
  }

  @Override
  public byte getByte(long address) {
    return slab.getByte(address);
  }

  @Override
  public MemoryView get(long address, int sizeBytes) {
    return slab.get(address, sizeBytes);
  }

  @Override
  public void visitBytes(long address, long sizeBytes, BytesVisitor bytesVisitor) {
    slab.visitBytes(address, sizeBytes, bytesVisitor);
  }

  @Override
  public void putByte(long address, byte b) {
    slab.putByte(address, b);
  }

  @Override
  public void put(long address, ByteStream byteStream) {
    slab.put(address, byteStream);
  }

  @Override
  public long getSize() {
    return slab.getSize();
  }

  @Override
  public void freeSlab() {
    slab.freeSlab();
  }

  @Override
  public long expand(long sizeBytes) throws FailedAllocationException {
    return slab.expand(sizeBytes);
  }

  protected Slab getSlab() {
    return slab;
  }

  @Override
  public String toString() {
    return "WrappedSlab{" +
      "slab=" + slab +
      '}';
  }
}
