package com.facebook.memory.slabs;

import com.facebook.memory.FailedAllocationException;
import com.facebook.memory.views.ByteStream;
import com.facebook.memory.views.BytesVisitor;
import com.facebook.memory.views.MemoryView;

/**
 * sits atop another slab and provides bounded access to Segment
 */
// TODO: not sure if we need this?
public class SlabSegment implements Slab {
  @Override
  public long allocate(long sizeBytes) throws FailedAllocationException {
    return 0;
  }

  @Override
  public long getUsed() {
    return 0;
  }

  @Override
  public void free(long address, int size) {

  }

  @Override
  public long getBaseAddress() {
    return 0;
  }

  @Override
  public byte getByte(long address) {
    return 0;
  }

  @Override
  public MemoryView get(long address, int sizeBytes) {
    return null;
  }

  @Override
  public void visitBytes(long address, long sizeBytes, BytesVisitor bytesVisitor) {

  }

  @Override
  public void putByte(long address, byte b) {

  }

  @Override
  public void put(long address, ByteStream byteStream) {

  }

  @Override
  public long getSize() {
    return 0;
  }

  @Override
  public void freeSlab() {

  }

  @Override
  public long expand(long sizeBytes) throws FailedAllocationException {
    return 0;
  }
}
