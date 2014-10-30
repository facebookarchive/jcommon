package com.facebook.memory.views;

/**
 * overlay of a contiguous space of memory. Provides arbitrary read/writes as well as iterator-style read and write
 * evolving: the set of types that are readable/writable from byteOffset, if offsets will be word-aligned
 */
public interface MemoryView extends ReadableMemoryView {
  boolean hasNextByte();

  byte nextByte();

  short nextShort();

  int nextInt();

  long nextLong();

  long nextPointer();

  byte nextByte(byte b);

  short nextShort(short s);

  int nextInt(int i);

  long nextLong(long l);

  long nextPointer(long p);

  void putByte(int byteOffset, byte b);

  void putShort(int byteOffset, short s);

  void putInt(int byteOffset, int i);

  void putLong(int byteOffset, long l);

  void putPointer(int byteOffset, long p);

}
