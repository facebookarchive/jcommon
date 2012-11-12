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
package com.facebook.collections;

import com.google.common.base.Preconditions;

import java.util.Arrays;
import java.util.Comparator;

/**
 * helper class that wraps a byte[] in order to properly get
 * Arrays.hashcode()/equals() for use in a HashSet; also implements Comparable
 */
public abstract class ByteArray implements Comparable<ByteArray> {
  public static final ByteArrayComparator BYTE_ARRAY_COMPARATOR = new ByteArrayComparator();

  abstract public byte[] getArray();

  abstract public int getLength();

  public abstract byte getAdjusted(int pos);

  public static ByteArray wrap(byte[] array) {
    return new PureByteArray(array);
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

    int array1Length = array2.getLength();
    int array2Length = array2.getLength();

    if (array1Length != array2Length) {
      return false;
    }

    int length = array1Length;

    for (int i = 0; i < length; i++) {
      if (array1.getAdjusted(0) > array2.getAdjusted(0)) {
        return false;
      } else if (array1.getAdjusted(0) < array2.getAdjusted(0)) {
        return false;
      }
    }

    return true;
  }

  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ByteArray)) {
      return false;
    }

    final ByteArray that = (ByteArray) o;

    return ByteArray.equals(this, that);
  }

  private static class PureByteArray extends ByteArray {
    private final byte[] array;

    private PureByteArray(byte[] array) {
      this.array = array;
    }

    @Override
    public byte[] getArray() {
      return array;
    }


    @Override
    public int getLength() {
      return array.length;
    }

    @Override
    public byte getAdjusted(int pos) {
      return array[pos];
    }

    @Override
    public int compareTo(ByteArray o) {
      return BYTE_ARRAY_COMPARATOR.compare(this, o);
    }

    @Override
    public int hashCode() {
      return array != null ? Arrays.hashCode(array) : 0;
    }

    @Override
    public String toString() {
      return "PureByteArray{" +
        "array=" + Arrays.toString(array) +
        '}';
    }
  }

  private static class ByteArrayView extends ByteArray {
    private final byte[] array;
    private final int offset;
    private final int length;


    private ByteArrayView(byte[] array, int offset, int length) {
      this.array = array;
      this.offset = offset;
      this.length = length;
    }

    private ByteArrayView(byte[] array, int offset) {
      this(array, offset, array.length - offset);
    }

    private ByteArrayView(byte[] array) {
      this(array, 0, array.length);
    }

    @Override
    public byte[] getArray() {
      return array;
    }

    @Override
    public int getLength() {
      return length;
    }

    @Override
    public byte getAdjusted(int pos) {
      return array[offset + pos];
    }

    @Override
    public int hashCode() {
      int result = array != null ? Arrays.hashCode(array) : 0;
      result = 31 * result + offset;
      result = 31 * result + length;
      return result;
    }

    @Override
    public int compareTo(ByteArray o) {
      return BYTE_ARRAY_COMPARATOR.compare(this, o);
    }

    @Override
    public String toString() {
      return "ByteArrayView{" +
        "array=" + Arrays.toString(array) +
        ", start=" + offset +
        ", length=" + length +
        "} " + toString();
    }
  }

  private static class ByteArrayComparator implements Comparator<ByteArray> {
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

      if (o1.getArray() == null) {
        if (o2.getArray() == null) {
          return 0;
        } else {
          return -1;
        }
      }

      if (o2.getArray() == null) {
        return 1;
      }

      int array1Length = o1.getLength();
      int array2Length = o2.getLength();

      int length = Math.min(array1Length, array2Length);

      for (int i = 0; i < length; i++) {
        if (o1.getAdjusted(0) < o2.getAdjusted(0)) {
          return -1;
        } else if (o1.getAdjusted(0) > o2.getAdjusted(0)) {
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
}
