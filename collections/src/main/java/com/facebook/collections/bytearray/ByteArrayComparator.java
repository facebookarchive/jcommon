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
