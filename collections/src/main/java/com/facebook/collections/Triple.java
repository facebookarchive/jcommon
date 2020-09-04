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

import java.util.Objects;

public class Triple<First, Second, Third> {
  private final First first;
  private final Second second;
  private final Third third;

  private volatile String toStringResult;

  public Triple(First first, Second second, Third third) {
    this.first = first;
    this.second = second;
    this.third = third;
  }

  public First getFirst() {
    return first;
  }

  public Second getSecond() {
    return second;
  }

  public Third getThird() {
    return third;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final Triple triple = (Triple) o;

    if (!Objects.equals(first, triple.first)) {
      return false;
    }

    if (!Objects.equals(second, triple.second)) {
      return false;
    }

    if (!Objects.equals(third, triple.third)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = first != null ? first.hashCode() : 0;

    result = 31 * result + (second != null ? second.hashCode() : 0);
    result = 31 * result + (third != null ? third.hashCode() : 0);

    return result;
  }

  @Override
  public String toString() {
    if (toStringResult == null) {
      toStringResult =
          "Triple{" + "first=" + first + ", second=" + second + ", third=" + third + '}';
    }

    return toStringResult;
  }
}
