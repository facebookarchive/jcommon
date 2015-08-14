package com.facebook.memory.slabs;

import com.facebook.memory.FailedAllocationException;
import com.facebook.memory.views.ByteStream;
import com.facebook.memory.views.BytesVisitor;
import com.facebook.memory.views.MemoryView;

public class ThreadLocalSlab implements Slab {
  private final int tlabSize;
  private final Slab slab;
  private final ThreadLocal<AllocateOnlySlab> tlab = new ThreadLocal<>();

  public ThreadLocalSlab(int tlabSize, Slab slab) {
    this.tlabSize = tlabSize;
    this.slab = slab;
  }

  @Override
  public Allocation tryAllocate(int sizeBytes) {
    Slab thisSlab = getSlabForAllocation(sizeBytes);

    return thisSlab.tryAllocate(sizeBytes);
  }

  @Override
  public long allocate(int sizeBytes) throws FailedAllocationException {
    Slab thisSlab = getSlabForAllocation(sizeBytes);

    return thisSlab.allocate(sizeBytes);
  }

  /**
   * finds a slab to do allocation by the following steps:
   *
   * 1. returns the backing slab if size is too big
   * 2. tries current TLAB
   * 3. will refresh the tlab if inadequate room
   */
  private Slab getSlabForAllocation(long sizeBytes) {
    Slab thisTlab = getThisTlab();

    // allocation won't fit in a tlab, return backing slab
    if (sizeBytes > thisTlab.getSize()) {
      return slab;
    }
    // tlab is too small, but object will fit in a new one
    if (sizeBytes > thisTlab.getFree()) {
      return refreshThisTlab();
    }
    // object fits in this tlab's free space
    return thisTlab;
  }

  /**
   * this will be an over-approximation: its the amount of memory used from this TLS perspective. It includes
   * all memory it has allocated and used, plus any "reserved" by other TLSs.
   *
   * @return
   */
  @Override
  public long getUsed() {
    return slab.getUsed() - getThisTlab().getFree();
  }

  /**
   * this is not strictly free memory in the system; it is from this TLS's perspective.
   * It inclueds free memory in this TLS as well as unallocated memory in the backing slab.
   *
   * @return
   */
  @Override
  public long getFree() {
    // the free memory from this thread's view: global + local
    long free2 = getThisTlab().getFree();
    long free = slab.getFree() + free2;
    return free;
  }

  @Override
  public void free(long address, int size) {
    // TODO+ need to guard against freeing memory that hasn't been allocated in the TLSlab layer
    slab.free(address, size);
  }

  @Override
  public long getBaseAddress() {
    return getThisTlab().getBaseAddress();
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
    return slab.getSize() + getThisTlab().getSize();
  }

  @Override
  public void freeSlab() {
    slab.freeSlab();
  }

  @Override
  public long expand(long sizeBytes) throws FailedAllocationException {
    return slab.expand(sizeBytes);
  }

  @SuppressWarnings("NumericCastThatLosesPrecision")
  private AllocateOnlySlab refreshThisTlab() {
    AllocateOnlySlab thisTlab = tlab.get();
    // AllocateOnlySlab.getUsed() will be the offset of unused memory; return to the underlying slab
    if (thisTlab.getFree() > 0) {
      slab.free(thisTlab.getBaseAddress() + thisTlab.getUsed(), (int) thisTlab.getFree());
    }
    tlab.set(allocateThisTlab());

    return tlab.get();
  }

  private Slab getThisTlab() {
    AllocateOnlySlab thisTlab = tlab.get();

    if (thisTlab == null) {
      AllocateOnlySlab newThisTlab = allocateThisTlab();

      tlab.set(newThisTlab);

      return newThisTlab;
    } else {
      return thisTlab;
    }
  }

  private AllocateOnlySlab allocateThisTlab() {
    Allocation allocation = slab.tryAllocate(tlabSize);
    // note: this may return a slab of size 0;  this is to allow for tryAllocate() to allocate 0 bytes and return
    // w/o exception
    return new AllocateOnlySlab(allocation.getAddress(), slab, allocation.getSize());
  }
}
