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

import com.google.common.base.Preconditions;

import java.util.Comparator;

public class ByteArrays {
  static final Comparator<ByteArray> BYTE_ARRAY_COMPARATOR = new ByteArrayComparator();

  static final AbstractByteArray NULL_BYTE_ARRAY = new NullByteArray();

  public static AbstractByteArray wrap(byte[] array) {
    if (array == null) {
      return ByteArrays.nullByteArray();
    } else {
      return new PureByteArray(array);
    }
  }

  public static AbstractByteArray nullByteArray() {
    return NULL_BYTE_ARRAY;
  }

  public static ByteArray wrap(byte[] array, int offset) {
    return new ByteArrayView(array, offset);
  }

  public static ByteArray wrap(byte[] array, int offset, int length) {
    Preconditions.checkArgument(offset + length <= array.length);
    return new ByteArrayView(array, offset, length);
  }

  public static boolean equals(ByteArray array1, ByteArray array2) {
    if (array1 == null) {
      if (array2 == null) {
        return true;
      } else {
        return false;
      }
    }
    if (array2 == null) {
      return false;
    }
    if (array1.isNull()) {
      if (array2.isNull()) {
        return true;
      } else {
        return false;
      }
    }

    if (array2.isNull()) {
      return false;
    }

    int array1Length = array1.getLength();
    int array2Length = array2.getLength();

    if (array1Length != array2Length) {
      return false;

    }

    for (int i = 0; i < array1Length; i++) {
      byte b1 = array1.getAdjusted(i);
      byte b2 = array2.getAdjusted(i);

      if (b1 != b2) {
        return false;
      }
    }

    return true;
  }

  public static int hashCode(ByteArray byteArray) {
    if (byteArray.isNull()) { return 0; }

    int result = 1;
    for (byte element : byteArray) { result = 31 * result + element; }

    return result;

  }

}
