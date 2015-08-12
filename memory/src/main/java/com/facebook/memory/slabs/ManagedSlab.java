package com.facebook.memory.slabs;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

import java.util.concurrent.atomic.AtomicInteger;

import com.facebook.memory.FailedAllocationException;
import com.facebook.memory.data.structures.freelists.FreeList;
import com.facebook.memory.data.structures.freelists.TreeSetFreeList;
import com.facebook.memory.views.ByteStream;
import com.facebook.memory.views.BytesVisitor;
import com.facebook.memory.views.MemoryView;

public class ManagedSlab implements Slab {
  private final long baseAddress;
  private final RawSlab slab;
  private final int managedSlabSize;
  private final FreeList freeList;
  private final AtomicInteger bytesUsed = new AtomicInteger(0);

  public ManagedSlab(long baseAddress, RawSlab slab, int managedSlabSize, FreeList freeList) {
    this.baseAddress = baseAddress;
    this.slab = slab;
    this.managedSlabSize = managedSlabSize;
    this.freeList = freeList;
  }

  public ManagedSlab(long baseAddress, RawSlab slab, int managedSlabSize) {
    this(baseAddress, slab, managedSlabSize, new TreeSetFreeList(managedSlabSize));
  }

  public static ManagedSlab fromRawSlab(RawSlab slab) {
    return new ManagedSlab(slab.getBaseAddress(), slab, Slabs.validateSize(slab.getSize()));
  }

  @Override
  public long getBaseAddress() {
    return baseAddress;
  }

  @Override
  public Allocation tryAllocate(int sizeBytes) {
    int validatedSizeBytes = Slabs.validateSize(sizeBytes);
    Span span = freeList.tryAllocate(validatedSizeBytes);

    if (span.getSize() > 0) {
      // adjust if need be
      bytesUsed.addAndGet(span.getSize());
    }

    return new Allocation(baseAddress + span.getOffset(), span.getSize());
  }

  @Override
  public long allocate(int sizeBytes) throws FailedAllocationException {
    Preconditions.checkState(freeList.getSize() == getFree(), "1");
    long address = baseAddress + freeList.allocate(Slabs.validateSize(sizeBytes));
    bytesUsed.addAndGet(Slabs.validateSize(sizeBytes));
    Preconditions.checkState(freeList.getSize() == getFree(), "2");

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
    // due to atomic reservations in tryAllocate(), bytesUsed may exceed the max
    return Math.min(bytesUsed.get(), managedSlabSize);
  }

  @Override
  public long getFree() {
    return managedSlabSize - getUsed();
  }

  @Override
  public long getSize() {
    return managedSlabSize;
  }

  @Override
  public void free(long address, int size) {
    long offset = (address - baseAddress);
    Preconditions.checkArgument(offset >= 0, "offset %d < 0", offset);

    // TODO : remove
    long rangeSizeSum1 = freeList.getSize();
    long freeBytes1 = getFree();
    Preconditions.checkState(rangeSizeSum1 == freeBytes1);

    freeList.free(Slabs.validateSize(offset), size);
    bytesUsed.addAndGet(Slabs.validateSize(-size));

    // todo: remove
    long rangeSizeSum2 = freeList.getSize();
    long freeBytes2 = getFree();

    Preconditions.checkState(rangeSizeSum2 == freeBytes2);
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
