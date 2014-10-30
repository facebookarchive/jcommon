package com.facebook.memory.views;

public interface MemoryViewFactory {

  MemoryView wrap(long address, long size);

  MemoryView wrap(ReadableMemoryView memoryView);

  ReadableMemoryView wrapByte(long address);

  ReadableMemoryView wrapShort(long address);

  ReadableMemoryView wrapInt(long address);

  ReadableMemoryView wrapLong(long address);
}
