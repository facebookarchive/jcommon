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
