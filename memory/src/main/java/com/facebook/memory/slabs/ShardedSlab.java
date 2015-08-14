package com.facebook.memory.slabs;

import java.util.concurrent.atomic.AtomicLong;

import com.facebook.memory.FailedAllocationException;
import com.facebook.memory.views.ByteStream;
import com.facebook.memory.views.BytesVisitor;
import com.facebook.memory.views.MemoryView;

public class ShardedSlab implements Slab {
  private final SlabPool slabPool;
  private final AllocationPolicy allocationPolicy;

  public ShardedSlab(SlabPool slabPool, AllocationPolicyFactory allocationPolicyFactory) {
    this.allocationPolicy = allocationPolicyFactory.create(slabPool);
    this.slabPool = slabPool;
  }

  @Override
  public Allocation tryAllocate(int sizeBytes) {
    return allocationPolicy.tryAllocate(new SizeSlabAllocationFunction(sizeBytes));
  }

  @Override
  public long allocate(int sizeBytes) throws FailedAllocationException {
    long address = allocationPolicy.allocate(new SizeSlabAllocationFunction(sizeBytes));

    return address;
  }

  @Override
  public long getUsed() {
    final AtomicLong used = new AtomicLong(0);
    slabPool.forEach(slab -> used.addAndGet(slab.getUsed()));

    return used.get();
  }

  @Override
  public long getFree() {
    final AtomicLong free = new AtomicLong(0);

    slabPool.forEach(slab -> free.addAndGet(slab.getFree()));

    return free.get();
  }

  @Override
  public void free(long address, int size) {
    Slab slab = getSlab(address);

    slab.free(address, size);
  }

  @Override
  public long getBaseAddress() {
    throw new UnsupportedOperationException();
  }

  @Override
  public byte getByte(long address) {
    Slab slab = getSlab(address);

    return slab.getByte(address);
  }

  @Override
  public MemoryView get(long address, int sizeBytes) {
    Slab slab = getSlab(address);
    MemoryView memoryView = slab.get(address, sizeBytes);

    return memoryView;
  }

  @Override
  public void visitBytes(long address, long sizeBytes, BytesVisitor bytesVisitor) {
    Slab slab = getSlab(address);

    slab.visitBytes(address, sizeBytes, bytesVisitor);
  }

  @Override
  public void putByte(long address, byte b) {
    Slab slab = getSlab(address);

    slab.putByte(address, b);
  }

  @Override
  public void put(long address, ByteStream byteStream) {
    Slab slab = getSlab(address);

    slab.put(address, byteStream);
  }

  @Override
  public long getSize() {
    final AtomicLong size = new AtomicLong(0);

    slabPool.forEach(slab -> size.addAndGet(slab.getSize()));

    return size.get();
  }

  @Override
  public void freeSlab() {
    slabPool.forEach(RawSlab::freeSlab);
  }

  @Override
  public long expand(long sizeBytes) throws FailedAllocationException {
    long perShardSizeBytes = sizeBytes / slabPool.getSize();
    long newSizeBytes = 0;

    for (Slab slab : slabPool) {
      newSizeBytes += slab.expand(perShardSizeBytes);
    }

    return newSizeBytes;
  }

  @Override
  public String toString() {
    return "ShardedSlab{" +
      "slabPool=" + slabPool +
      '}';
  }

  private Slab getSlab(long address) {
    return slabPool.getSlabByAddress(address);
  }
}
