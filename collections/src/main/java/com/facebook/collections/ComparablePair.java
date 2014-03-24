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

public class ComparablePair
  <T1 extends Comparable<? super T1>, T2 extends Comparable<? super T2>>
  extends Pair<T1, T2> implements Comparable<ComparablePair<T1, T2>> {

  public ComparablePair(T1 t1, T2 t2) {
    super(t1, t2);
  }

  public static <T1 extends Comparable<? super T1>, T2 extends Comparable<? super T2>>
  Pair<T1, T2> of(T1 first, T2 second) {
    return new ComparablePair<>(first, second);
  }

  @Override
  public int compareTo(ComparablePair<T1, T2> o) {
    int firstCompareTo = getFirst().compareTo(o.getFirst());

    if (firstCompareTo == 0) {
      return getSecond().compareTo(o.getSecond());
    }

    return firstCompareTo;
  }

  // using Pair's equals()/hashCode() 
}
