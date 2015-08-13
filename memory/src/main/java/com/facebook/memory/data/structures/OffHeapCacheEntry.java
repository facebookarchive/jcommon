package com.facebook.memory.data.structures;

import com.facebook.memory.data.types.definitions.OffHeapStructure;

public interface OffHeapCacheEntry extends OffHeapStructure {
  long getPrevious();

  void setPrevious(long value);

  long getNext();

  void setNext(long value);

  long getDataPointer();

  void setDataPointer(long value);
}
