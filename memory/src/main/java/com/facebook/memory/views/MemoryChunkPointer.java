package com.facebook.memory.views;

import sun.misc.Unsafe;

import com.facebook.memory.FailedAllocationException;
import com.facebook.memory.slabs.Slab;
import com.facebook.memory.UnsafeAccessor;

public class MemoryChunkPointer {
  private static final int MEMORY_CHUNK_PTR_SIZE = 12; // 8 byte ptr, 4 byte max ByteView
  private static final Unsafe UNSAFE = UnsafeAccessor.get();

  private final long address;

  public MemoryChunkPointer(long address) {
    this.address = address;
  }

  public static Block block(Slab slab) {
    return new Block(slab, slab.getBaseAddress());
  }

  public long getDataAddress() {
    return UNSAFE.getAddress(address);
  }

  public long getDataSize() {
    return UNSAFE.getInt(address + Long.BYTES);
  }

  private MemoryChunkPointer setDataAddress(long dataAddress) {
    UNSAFE.putAddress(address, dataAddress);

    return this;
  }

  private MemoryChunkPointer setDataSize(int dataSize) {
    UNSAFE.putInt(address, dataSize);

    return this;
  }

  public static class Block {
    private final Slab slab;
    private long current;

    private Block(Slab slab, long current) {
      this.slab = slab;
      this.current = current;
    }

    public boolean hasNext() {
      return current < slab.getBaseAddress() + slab.getSize();
    }

    public MemoryChunkPointer read() {
      MemoryChunkPointer memoryChunkPointer = new MemoryChunkPointer(current);

      current += MEMORY_CHUNK_PTR_SIZE;

      return memoryChunkPointer;
    }

    public MemoryChunkPointer append(long dataPtr, int dataSize) throws FailedAllocationException {
      long address = slab.allocate(MEMORY_CHUNK_PTR_SIZE);
      MemoryChunkPointer memoryChunkPointer = new MemoryChunkPointer(address)
        .setDataAddress(dataPtr)
        .setDataSize(dataSize);

      return memoryChunkPointer;
    }
  }
}
