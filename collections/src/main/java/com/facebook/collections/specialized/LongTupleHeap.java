package com.facebook.collections.specialized;

import com.facebook.collections.SimpleHeap;

/**
 * marker interface; also binds some return types more tightly (makeCopy)
 */
public interface LongTupleHeap extends SimpleHeap<long[]>{
  @Override
  LongTupleHeap makeCopy();
}
