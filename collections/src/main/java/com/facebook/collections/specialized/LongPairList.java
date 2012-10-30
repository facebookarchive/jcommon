package com.facebook.collections.specialized;


import java.util.Arrays;

/**
 * sorts tuples by their first element in ascending order. The first element may not be negative,
 * but subsequent ones may be
 */

public class LongPairList extends AbstractLongTupleList implements LongTupleHeap {

  private LongPairList(long[] tuples, int size) {
    super(tuples, size);
  }

  public LongPairList(int initialSize) {
    super(initialSize, 2);
  }

  @Override
  protected int getTupleSize() {
    return 2;
  }

  @Override
  protected LongTupleHeap copyHeap(long[] tuples, int size) {
    return new LongPairList(Arrays.copyOf(tuples, tuples.length), size);
  }
}
