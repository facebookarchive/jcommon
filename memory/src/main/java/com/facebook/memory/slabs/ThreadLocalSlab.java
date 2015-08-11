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
  public long allocate(long sizeBytes) throws FailedAllocationException {
    Slab thisTlab = getThisTlab();
    // allocation won't fit in a tlab
    if (thisTlab.getSize() < sizeBytes) {
      return slab.allocate(sizeBytes);
    }
    // tlab is too small, but object will fit in a new one
    if (thisTlab.getFree() < sizeBytes) {
      thisTlab = refreshThisTlab();
    }

    return thisTlab.allocate(sizeBytes);
  }

  @Override
  public long getUsed() {
    return slab.getUsed() - getThisTlab().getFree();
  }

  @Override
  public long getFree() {
    // the free memory from this thread's view: global + local
    long free2 = getThisTlab().getFree();
    long free = slab.getFree() + free2;
    return free;
  }

  @Override
  public void free(long address, int size) {
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

  private AllocateOnlySlab refreshThisTlab() throws FailedAllocationException {
    // AllocateOnlySlab.getUsed() will be the offset of unused memory; return to the underlying slab
    if (tlab.get().getFree() > 0) {
      slab.free(slab.getBaseAddress() + slab.getUsed(), (int) slab.getFree());
    }

    tlab.set(allocateThisTlab());

    return tlab.get();
  }

  private Slab getThisTlab() {
    AllocateOnlySlab thisTlab = tlab.get();

    if (thisTlab == null) {
      try {
        AllocateOnlySlab newThisTlab = allocateThisTlab();

        tlab.set(newThisTlab);

        return newThisTlab;
      } catch (FailedAllocationException e) {
        // TODO : figure this out...
        throw new RuntimeException(e);
      }
    } else {
      return thisTlab;
    }
  }

  private AllocateOnlySlab allocateThisTlab() throws FailedAllocationException {
    long tlabAddress = slab.allocate(tlabSize);

    return new AllocateOnlySlab(tlabAddress, slab, tlabSize);
  }
}
