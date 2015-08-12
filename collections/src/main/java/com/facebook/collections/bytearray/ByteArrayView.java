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

import java.util.Arrays;

class ByteArrayView extends AbstractByteArray {
  private final byte[] array;
  private final int offset;
  private final int length;

  ByteArrayView(byte[] array, int offset, int length) {
    this.array = array;
    this.offset = offset;
    this.length = length;
  }

  ByteArrayView(byte[] array, int offset) {
    this(array, offset, array.length - offset);
  }

  ByteArrayView(byte[] array) {
    this(array, 0, array.length);
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
  public void putAdjusted(int pos, byte b) {
    array[offset + pos] = b;
  }

  @Override
  public boolean isNull() {
    return array == null;
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
