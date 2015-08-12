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

public class WrappedByteArray extends AbstractByteArray {
  private final ByteArray byteArray;

  public WrappedByteArray(ByteArray byteArray) {
    this.byteArray = byteArray;
  }

  @Override
  public int getLength() {return byteArray.getLength();}

  @Override
  public byte getAdjusted(int pos) {return byteArray.getAdjusted(pos);}

  @Override
  public void putAdjusted(int pos, byte b) {byteArray.putAdjusted(pos, b);}

  @Override
  public boolean isNull() {return byteArray.isNull();}
}
