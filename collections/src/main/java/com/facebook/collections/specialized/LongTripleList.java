package com.facebook.collections.specialized;


import java.util.Arrays;

/**
 * sorts tuples by their first element in ascending order. The first element may not be negative,
 * but subsequent ones may be
 */

public class LongTripleList extends AbstractLongTupleList implements LongTupleHeap {

  private LongTripleList(long[] tuples, int size) {
    super(tuples, size);
  }

  public LongTripleList(int initialSize) {
    super(initialSize, 3);
  }

  @Override
  protected int getTupleSize() {
    return 3;
  }

  @Override
  protected LongTupleHeap copyHeap(long[] tuples, int size) {
    return new LongTripleList(Arrays.copyOf(tuples, tuples.length), size);
  }
}
