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
package com.facebook.collections.specialized;

import com.facebook.collections.ByteArray;

import java.io.ByteArrayOutputStream;

/**
 * allows direct access to the underlying buffer without a copy
 * also has a higher default size
 */
public class MutableByteArrayOutputStream extends ByteArrayOutputStream {
  
  public MutableByteArrayOutputStream() {
    // use a higher default size
    this(1024);
  }

  public MutableByteArrayOutputStream(int size) {
    super(size);
  }
  
  public ByteArray getBytes() {
    return ByteArray.wrap(buf, 0, size());
  }
}
