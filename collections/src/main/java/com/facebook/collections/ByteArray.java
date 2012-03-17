package com.facebook.collections;

import java.util.Arrays;

/**
 * helper class that wraps a byte[] in order to properly get 
 * Arrays.hashcode()/equals() for use in a HashSet; also implements Comparable
 */
public class ByteArray implements Comparable<ByteArray>{
  private final byte[] array;

  public ByteArray(byte[] array) {
    this.array = array;
  }

  public byte[] getArray() {
    return array;
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(array);
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof ByteArray && 
      Arrays.equals(array, ((ByteArray)obj).array);
  }

  @Override
  public int compareTo(ByteArray o) {
    // null < all other values
    if (o == null) {
      return 1;
    } else if (array == null) {
      if (o.array == null) {
        return 0;
      } else {
        return -1;
      }
    } else if (o.array == null) {
      return 1;
    }
    
    int len = Math.min(array.length, o.array.length);

    for (int i = 0; i < len; i++) {
      if (array[i] < o.array[i]) {
        return -1;
      } else if (array[i] > o.array[i]) {
        return 1;
      }
    }

    if (array.length < o.array.length) {
      return -1;
    } else if (array.length > o.array.length) {
      return 1;
    }

    // equal size & contents
    return 0;
  }
}
