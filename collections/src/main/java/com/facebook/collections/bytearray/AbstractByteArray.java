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
package com.facebook.collections.bytearray;

import com.google.common.collect.AbstractIterator;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Minimal interface to represent a sequence of bytes. This may be backed by byte[], offheap storage, or another other
 * random-accessible sequence of bytes
 * <p>
 * Implements hashCode, equals, and Comparable in order to allow use in various data structs.
 * Also implements Iterable<Byte> (never returns null)
 */
public abstract class AbstractByteArray implements ByteArray {

  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AbstractByteArray)) {
      return false;
    }

    final ByteArray that = (ByteArray) o;

    return ByteArrays.equals(this, that);
  }

  @Override
  public int hashCode() {
    return ByteArrays.hashCode(this);
  }

  @Override
  public int compareTo(ByteArray o) {
    return ByteArrays.BYTE_ARRAY_COMPARATOR.compare(this, o);
  }

  @Override
  public Iterator<Byte> iterator() {
    return new AbstractIterator<Byte>() {
      private final int length = getLength();
      private final AtomicInteger position = new AtomicInteger(0);

      @Override
      protected Byte computeNext() {
        int currentPosition = position.getAndIncrement();

        if (currentPosition >= length) {
          return endOfData();
        } else {
          return getAdjusted(currentPosition);
        }
      }
    };
  }

    // expensive
  protected String arrayToString() {
    byte[] buf = new byte[getLength()];
    int i = 0;

    for (byte b : this) {
      buf[i++] = b;
    }

    return Arrays.toString(buf);
  }

}
