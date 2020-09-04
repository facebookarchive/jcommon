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
package com.facebook.collectionsbase;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class Lists {
  private Lists() {
    throw new AssertionError();
  }

  public static <T extends Comparable<? super T>> int compareLists(
      List<? extends T> list1, List<? extends T> list2) {
    Comparator<T> comparator = (o1, o2) -> o1.compareTo(o2);

    return compareLists(list1, list2, comparator);
  }

  /**
   * compares lists lexicographically as though elements are characters in an alphabet that are
   * ordered by comparator
   *
   * @param list1
   * @param list2
   * @param comparator
   * @param <T>
   * @return -1, 0, 1 according to Comparator specs
   */
  public static <T> int compareLists(
      List<? extends T> list1, List<? extends T> list2, Comparator<? super T> comparator) {
    Iterator<? extends T> iter1 = list1.iterator();
    Iterator<? extends T> iter2 = list2.iterator();

    while (iter1.hasNext() && iter2.hasNext()) {
      T item1 = iter1.next();
      T item2 = iter2.next();

      int result = comparator.compare(item1, item2);

      if (result != 0) {
        return Integer.signum(result);
      }
    }

    return iter1.hasNext() ? 1 : (iter2.hasNext() ? -1 : 0);
  }

  /**
   * @param array1
   * @param array2
   * @return
   */
  public static int compareArrays(byte[] array1, byte[] array2) {
    int minLength = Math.min(array1.length, array2.length);
    for (int i = 0; i < minLength; i++) {
      int result = (array1[i] - array2[i]);

      if (result != 0) {
        return Integer.signum(result);
      }
    }

    if (array1.length == array2.length) {
      return 0;
    } else if (array1.length > array2.length) {
      return 1;
    } else {
      return -1;
    }
  }

  public static <T extends Comparable<? super T>> int compareArrays(T[] array1, T[] array2) {
    Comparator<T> comparator = (o1, o2) -> o1.compareTo(o2);

    return compareArrays(array1, array2, comparator);
  }

  public static <T> int compareArrays(T[] array1, T[] array2, Comparator<? super T> comparator) {
    for (int i = 0; i < array1.length && i < array2.length; i++) {
      int result = comparator.compare(array1[i], array2[i]);

      if (result != 0) {
        return Integer.signum(result);
      }
    }

    if (array1.length == array2.length) {
      return 0;
    } else if (array1.length > array2.length) {
      return 1;
    } else {
      return -1;
    }
  }
}
