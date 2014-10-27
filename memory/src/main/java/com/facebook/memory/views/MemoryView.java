package com.facebook.memory.views;

/**
 * overlay of a contiguous space of memory. Provides arbitrary read/writes as well as iterator-style read and write
 * evolving: the set of types that are readable/writable from byteOffset, if offsets will be word-aligned
 */
public interface MemoryView {
  public boolean hasNextByte();

  public byte nextByte();

  public short nextShort();

  public int nextInt();

  public long nextLong();

  public long nextPointer();

  public byte nextByte(byte b);

  public short nextShort(short s);

  public int nextInt(int i);

  public long nextLong(long l);

  public long nextPointer(long p);

  public byte getByte(int byteOffset);

  public short getShort(int byteOffset);

  public int getInt(int byteOffset);

  public long getLong(int byteOffset);

  public long getPointer(int byteOffset);

  public void putByte(int byteOffset, byte b);

  public void putShort(int byteOffset, short s);

  public void putInt(int byteOffset, int i);

  public void putLong(int byteOffset, long l);

  public void putPointer(int byteOffset, long p);

  public long getAddress();

  public long getSize();

  public long getCurrent();

  public long getMaxSize();
}
