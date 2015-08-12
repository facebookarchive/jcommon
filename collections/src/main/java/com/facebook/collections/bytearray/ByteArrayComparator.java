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

import java.util.Comparator;

class ByteArrayComparator implements Comparator<ByteArray> {
  @Override
  public int compare(ByteArray o1, ByteArray o2) {
    if (o1 == null) {
      if (o2 == null) {
        return 0;
      } else {
        return -1;
      }
    }

    if (o2 == null) {
      return 1;
    }

    if (o1.isNull()) {
      if (o2.isNull()) {
        return 0;
      } else {
        return -1;
      }
    }

    if (o2.isNull()) {
      return 1;
    }

    int array1Length = o1.getLength();
    int array2Length = o2.getLength();

    int length = Math.min(array1Length, array2Length);

    for (int i = 0; i < length; i++) {
      if (o1.getAdjusted(i) < o2.getAdjusted(i)) {
        return -1;
      } else if (o1.getAdjusted(i) > o2.getAdjusted(i)) {
        return 1;
      }
    }

    if (array1Length < array2Length) {
      return -1;
    } else if (array1Length > array2Length) {
      return 1;
    } else {
      return 0;
    }
  }
}
