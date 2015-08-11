package com.facebook.memory.slabs;

import com.facebook.memory.FailedAllocationException;
import com.facebook.memory.views.ByteStream;
import com.facebook.memory.views.BytesVisitor;
import com.facebook.memory.views.MemoryView;

public class SynchronizedSlab implements Slab {
  private final Slab slab;

  public SynchronizedSlab(Slab slab) {
    this.slab = slab;
  }

  @Override
  public synchronized long allocate(long sizeBytes) throws FailedAllocationException {
    return slab.allocate(sizeBytes);
  }

  @Override
  public synchronized long getUsed() {
    return slab.getUsed();
  }

  @Override
  public long getFree() {
    return slab.getFree();
  }

  @Override
  public synchronized void free(long address, int size) {
    slab.free(address, size);
  }

  @Override
  public synchronized long getBaseAddress() {
    return slab.getBaseAddress();
  }

  @Override
  public synchronized byte getByte(long address) {
    return slab.getByte(address);
  }

  @Override
  public synchronized MemoryView get(long address, int sizeBytes) {
    return slab.get(address, sizeBytes);
  }

  @Override
  public synchronized void visitBytes(long address, long sizeBytes, BytesVisitor bytesVisitor) {
    slab.visitBytes(address, sizeBytes, bytesVisitor);
  }

  @Override
  public synchronized void putByte(long address, byte b) {
    slab.putByte(address, b);
  }

  @Override
  public synchronized void put(long address, ByteStream byteStream) {
    slab.put(address, byteStream);
  }

  @Override
  public synchronized long getSize() {
    return slab.getSize();
  }

  @Override
  public synchronized void freeSlab() {
    slab.freeSlab();
  }

  @Override
  public synchronized long expand(long sizeBytes) throws FailedAllocationException {
    return slab.expand(sizeBytes);
  }

  @Override
  public String toString() {
    return "SynchronizedSlab{" +
      "slab=" + slab +
      '}';
  }
}
