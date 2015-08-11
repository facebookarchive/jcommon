package com.facebook.memory.slabs;

import com.facebook.memory.FailedAllocationException;
import com.facebook.memory.views.ByteStream;
import com.facebook.memory.views.BytesVisitor;
import com.facebook.memory.views.MemoryView;

/**
 * a raw chunk of memory that can be read from, written to, iterated.
 */
interface RawSlab {
  long getBaseAddress();

  byte getByte(long address);

  MemoryView get(long address, int sizeBytes);

  void visitBytes(long address, long sizeBytes, BytesVisitor bytesVisitor);

  void putByte(long address, byte b);

  void put(long address, ByteStream byteStream);

  long getSize();

  void freeSlab();

  /**
   * attempts to grow a Slab to sizeBytes. Previous size must be smaller.  May not grow 100%, check return
   * value for actual new size
   *
   * @param sizeBytes
   * @return actual new size
   * @throws com.facebook.memory.FailedAllocationException
   */
  long expand(long sizeBytes) throws FailedAllocationException;
}
