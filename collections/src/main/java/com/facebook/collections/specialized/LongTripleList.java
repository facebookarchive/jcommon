/*
 * Copyright (C) 2012 Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
