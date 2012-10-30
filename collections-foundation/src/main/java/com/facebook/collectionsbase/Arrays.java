package com.facebook.collectionsbase;

import java.util.Comparator;

public class Arrays {
  private Arrays() {
    throw new AssertionError();
  }

  /**
   * sorts in ocmparator order, usually ascending
   *
   * @param array1
   * @param array2
   * @param <T>
   * @return
   */
  public static <T extends Comparable<? super T>> int compareArrays(T[] array1, T[] array2) {
    Comparator<T> comparator = new Comparator<T>() {
      @Override
      public int compare(T o1, T o2) {
        return o1.compareTo(o2);
      }
    };

    return compareArrays(array1, array2, comparator);
  }


  public static <T> int comparPrimitiveArrays(
    T[] array1, T[] array2, Comparator<? super T[]> comparator
  ) {

    int compare = comparator.compare(array1, array2);

    return compare;
  }

  /**
   * compares arrays by the specified comparator
   *
   * @param array1
   * @param array2
   * @param <T>
   * @return
   */
  public static <T> int compareArrays(
    T[] array1, T[] array2, Comparator<? super T> comparator

  ) {
    // null < all other values
    if (array1 == null) {
      if (array2 == null) {
        return 0;
      } else {
        return -1;
      }
    } else if (array2 == null) {
      return 1;
    } else {

      int len = Math.min(array1.length, array2.length);

      for (int i = 0; i < len; i++) {
        int compareValue = comparator.compare(array1[i], array2[i]);

        if (compareValue != 0) {
          return compareValue;
        }
      }

      if (array1.length > array2.length) {
        return 1;
      } else if (array1.length < array2.length) {
        return -1;
      } else {
        return 0;
      }
    }
  }
}
