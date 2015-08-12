package com.facebook.memory.slabs;

import java.util.concurrent.atomic.AtomicLong;

import com.facebook.memory.FailedAllocationException;
import com.facebook.memory.views.ByteStream;
import com.facebook.memory.views.BytesVisitor;
import com.facebook.memory.views.MemoryView;

public class RawSlabAdapter implements Slab {
  private final RawSlab slab;
  private final AtomicLong offset = new AtomicLong(0);

  public RawSlabAdapter(RawSlab slab) {
    this.slab = slab;
  }

  @Override
  public Allocation tryAllocate(int sizeBytes) {
    AddWithMaxResult addWithMaxResult = Slabs.allocateFromAtomicLong(offset, sizeBytes, slab.getSize());

    return new Allocation(addWithMaxResult.getPreviousValue(), addWithMaxResult.getActualDelta());
  }

  @Override
  public long allocate(int sizeBytes) throws FailedAllocationException {
    return slab.getBaseAddress() + offset.getAndAdd(sizeBytes);
  }

  @Override
  public long getUsed() {
    return offset.get();
  }

  @Override
  public long getFree() {
    return slab.getSize() - getUsed();
  }

  @Override
  public void free(long address, int size) {
    // no-op
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

  @Override
  public String toString() {
    return "RawSlabAdapter{" +
      "slab=" + slab +
      ", offset=" + offset +
      '}';
  }
}
