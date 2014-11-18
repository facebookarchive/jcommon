/*
 * Copyright (C) 2014 Facebook, Inc.
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
package com.facebook.tools.io;

class CharArraySequence implements CharSequence {
  private final char[] array;
  private final int offset;
  private final int length;

  CharArraySequence(char[] array) {
    this(array, 0, array.length);
  }

  CharArraySequence(char[] array, int offset, int length) {
    this.array = array;
    this.offset = offset;
    this.length = length;
  }

  @Override
  public int length() {
    return length;
  }

  @Override
  public char charAt(int index) {
    if (index < 0 || index > length) {
      throw new IndexOutOfBoundsException();
    }

    return array[offset + index];
  }

  @Override
  public CharSequence subSequence(int start, int end) {
    if (start < 0 || end < 0 || start > end || end > length) {
      throw new IndexOutOfBoundsException();
    }

    return start == end ? "" : new CharArraySequence(array, offset + start, end - start);
  }

  @Override
  public String toString() {
    return new String(array, offset, length);
  }
}
