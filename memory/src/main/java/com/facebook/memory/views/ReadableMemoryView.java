package com.facebook.memory.views;

import com.facebook.memory.data.structures.OffHeap;

public interface ReadableMemoryView extends OffHeap {
  byte getByte();

  byte getByte(int byteOffset);

  short getShort();

  short getShort(int byteOffset);

  int getInt();

  int getInt(int byteOffset);

  long getLong();

  long getLong(int byteOffset);

  long getPointer();

  long getPointer(int byteOffset);

  long getSize();

  long getCurrent();

  long getMaxSize();

  MemoryView reset();

  MemoryView splice(long offset);

  MemoryView splice(long offset, long size);
}
