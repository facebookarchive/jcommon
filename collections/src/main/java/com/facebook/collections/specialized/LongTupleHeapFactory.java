package com.facebook.collections.specialized;

import com.facebook.collections.SimpleHeapFactory;
import com.facebook.collections.specialized.LongTupleHeap;

public class LongTupleHeapFactory implements SimpleHeapFactory<long[], LongTupleHeap> {
  private final int tupleSize;

  public LongTupleHeapFactory(int tupleSize) {
    this.tupleSize = tupleSize;
  }

  @Override
  public LongTupleHeap create(int initializeSize) {
    if (tupleSize == 2) {
      return new LongPairList(initializeSize);
    } else if (tupleSize == 3) {
      return new LongTripleList(initializeSize);
    } else {
      throw new IllegalStateException(
        String.format(
          "tuple size of %d attempted. Only 2 and 3 supported", tupleSize)
      );
    }

  }
}
