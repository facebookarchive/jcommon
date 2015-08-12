package com.facebook.memory.slabs;

import java.util.concurrent.atomic.AtomicInteger;

import com.facebook.memory.FailedAllocationException;
import com.facebook.memory.views.ByteStream;
import com.facebook.memory.views.BytesVisitor;
import com.facebook.memory.views.MemoryView;

public class AllocateOnlySlab implements Slab {
  private final long baseAddress;
  private final RawSlab slab;
  private final int slabSize;
  private final AtomicInteger offset = new AtomicInteger(0);

  public AllocateOnlySlab(long baseAddress, RawSlab slab, int slabSize) {
    this.baseAddress = baseAddress;
    this.slab = slab;
    this.slabSize = slabSize;
  }

  public static AllocateOnlySlab fromRawSlab(RawSlab slab) {
    return new AllocateOnlySlab(slab.getBaseAddress(), slab, Slabs.validateSize(slab.getSize()));
  }

  @Override
  public long getBaseAddress() {
    return baseAddress;
  }

  @Override
  public Allocation tryAllocate(int sizeBytes) {
    int validatedSizeBytes = Slabs.validateSize(sizeBytes);

    AddWithMaxResult addWithMaxResult = Slabs.allocateFromAtomicInteger(offset, validatedSizeBytes, slabSize);
    long address = baseAddress + addWithMaxResult.getPreviousValue();

    return new Allocation(address, addWithMaxResult.getActualDelta());
  }

  @Override
  public long allocate(int sizeBytes) throws FailedAllocationException {
    int validatedSizeBytes = Slabs.validateSize(sizeBytes);
    int preAllocatedSize = offset.getAndAdd(validatedSizeBytes);
    int postAllocationSize = validatedSizeBytes + preAllocatedSize;
    long address = baseAddress + preAllocatedSize;

    if (postAllocationSize > slabSize) {
      throw new FailedAllocationException(
        String.format(
          "allocation of size %d failed; slab size %d would exceed max %d",
          sizeBytes,
          postAllocationSize,
          slabSize
        )
      );
    }

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
    return Math.min(offset.get(), slabSize);
  }

  @Override
  public long getFree() {
    return slabSize - offset.get();
  }

  @Override
  public long getSize() {
    return slab.getSize();
  }

  @Override
  public void free(long address, int size) {
    throw new UnsupportedOperationException("cannot free memory in this class, use base slab");
  }

  @Override
  public long expand(long sizeBytes) throws FailedAllocationException {
    throw new UnsupportedOperationException("cannot expand");
  }

  @Override
  public void freeSlab() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String toString() {
    return "AllocateOnlySlab{" +
      "baseAddress=" + baseAddress +
      ", slab=" + slab +
      ", slabSize=" + slabSize +
      ", offset=" + offset +
      '}';
  }
}
