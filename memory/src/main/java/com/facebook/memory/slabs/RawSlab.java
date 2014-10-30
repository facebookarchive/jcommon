package com.facebook.memory.slabs;

import com.facebook.memory.FailedAllocationException;
import com.facebook.memory.views.ByteStream;
import com.facebook.memory.views.BytesVisitor;
import com.facebook.memory.views.MemoryView;

/**
 * a raw chunk of memory that can be read from, written to, iterated.
 */
public interface RawSlab {
  public long getBaseAddress();

  public byte getByte(long address);

  public MemoryView get(long address, int sizeBytes);

  public void visitBytes(long address, long sizeBytes, BytesVisitor bytesVisitor);

  public void putByte(long address, byte b);

  public void put(long address, ByteStream byteStream);

  public long getSize();

  public void freeSlab();

  /**
   * attempts to grow a Slab to sizeBytes. Previous size must be smaller.  May not grow 100%, check return
   * value for actual new size
   *
   * @param sizeBytes
   * @return actual new size
   * @throws com.facebook.memory.FailedAllocationException
   */
  public long expand(long sizeBytes) throws FailedAllocationException;
}
