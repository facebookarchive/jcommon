package com.facebook.memory.slabs;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

import java.util.concurrent.atomic.AtomicInteger;

import com.facebook.memory.FailedAllocationException;
import com.facebook.memory.data.structures.FreeList;
import com.facebook.memory.data.structures.TreeSetFreeList;
import com.facebook.memory.views.ByteStream;
import com.facebook.memory.views.BytesVisitor;
import com.facebook.memory.views.MemoryView;

public class ManagedSlab implements Slab {
  private final long baseAddress;
  private final RawSlab slab;
  private final int managedSlabSize;
  private final FreeList freeList;
  private final AtomicInteger bytesUsed = new AtomicInteger(0);

  public ManagedSlab(long baseAddress, RawSlab slab, int managedSlabSize) {
    this.baseAddress = baseAddress;
    this.slab = slab;
    this.managedSlabSize = managedSlabSize;
    freeList = new TreeSetFreeList(managedSlabSize);
  }

  public static ManagedSlab fromRawSlab(RawSlab slab) {
    return new ManagedSlab(slab.getBaseAddress(), slab, Slabs.validateSize(slab.getSize()));
  }

  @Override
  public long getBaseAddress() {
    return baseAddress;
  }

  @Override
  public long allocate(long sizeBytes) throws FailedAllocationException {
    long address = baseAddress + freeList.allocate(Slabs.validateSize(sizeBytes));

    bytesUsed.addAndGet(Slabs.validateSize(sizeBytes));

    return address;
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
  public long getUsed() {
    return bytesUsed.get();
  }

  @Override
  public long getFree() {
    return slab.getSize() - bytesUsed.get();
  }

  @Override
  public long getSize() {
    return slab.getSize();
  }

  @Override
  public void free(long address, int size) {
    long offset = (address - baseAddress);
    Preconditions.checkArgument(offset >= 0, "offset %d < 0", offset);

    freeList.free(Slabs.validateSize(offset), size);
    bytesUsed.addAndGet(Slabs.validateSize(-size));
  }

  @Override
  public long expand(long sizeBytes) throws FailedAllocationException {
    int intSizeBytes = Slabs.validateSize(sizeBytes);

    slab.expand(sizeBytes);
    freeList.extend(intSizeBytes);

    return slab.getSize();
  }

  @Override
  public void freeSlab() {
    freeList.reset(0);
    slab.freeSlab();
  }

  @Override
  public String toString() {
    return "ManagedSlab{" +
      "baseAddress=" + baseAddress +
      ", slab=" + slab +
      ", managedSlabSize=" + managedSlabSize +
      ", freeList=" + freeList +
      ", bytesUsed=" + bytesUsed +
      '}';
  }

  @VisibleForTesting
  FreeList getFreeList() {
    return freeList;
  }

  private void internalPut(long position, MemoryView memoryView) {
    while (memoryView.hasNextByte()) {
      slab.putByte(position, memoryView.nextByte());
    }
  }

  private boolean shouldCompact() {
    // TODO: detect when we should copy things to the head of the Slab
    return false;
  }

  private void compact() {
    // TODO: detect when we should copy things to the head of the Slab

  }
}
