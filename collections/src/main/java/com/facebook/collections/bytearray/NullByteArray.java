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

import java.util.NoSuchElementException;

class NullByteArray extends AbstractByteArray {
  @Override
  public int getLength() {
    return 0;
  }

  @Override
  public byte getAdjusted(int pos) {
    throw new NoSuchElementException("null ByteArray");
  }

  @Override
  public void putAdjusted(int pos, byte b) {
    throw new UnsupportedOperationException("cannot write to a null byte array");
  }

  @Override
  public boolean isNull() {
    return true;
  }

  @Override
  public int compareTo(ByteArray o) {
    // order is null, NullByteArray, [all others]
    if (o == null) {
      return 1;
    } else {
      return o.isNull() ? 0 : -1;
    }
  }
}
