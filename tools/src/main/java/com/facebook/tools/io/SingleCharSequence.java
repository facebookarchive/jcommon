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

class SingleCharSequence implements CharSequence {
  private final char character;

  SingleCharSequence(char character) {
    this.character = character;
  }

  @Override
  public int length() {
    return 1;
  }

  @Override
  public char charAt(int index) {
    if (index < 0 || index > 1) {
      throw new IndexOutOfBoundsException();
    }

    return character;
  }

  @Override
  public CharSequence subSequence(int start, int end) {
    if (start < 0 || end < 0 || start > end || end > 1) {
      throw new IndexOutOfBoundsException();
    }

    return end == 0 ? "" : this;
  }

  @Override
  public String toString() {
    return Character.toString(character);
  }
}
