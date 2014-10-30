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
package com.facebook.memory;

import org.testng.Assert;
import org.testng.annotations.Test;
import sun.misc.Unsafe;

public class TestUnsafeAccessor {
  /**
   * allocates 1MB, write a bunch of bytes, reads them back, frees the memory
   * @throws Exception
   */
  @Test(groups = "fast")
  public void testSanity() throws Exception {
  	Unsafe unsafe = UnsafeAccessor.get();

    int size = 1024 * 1024;
    long ptr = unsafe.allocateMemory(size);
    try {
      long writePtr = ptr;

      for (int i = 0; i < size; i++) {
        byte b = (byte)(i % 127);

        unsafe.putByte(writePtr, b);
        writePtr++;
      }

      long readPtr = ptr;

      for (int i = 0; i < size; i++) {
        byte b = (byte)(i % 127);

        byte readByte = unsafe.getByte(readPtr);
        readPtr++;

        Assert.assertEquals(b, readByte);
      }
    } finally {
      unsafe.freeMemory(ptr);
    }

  }
}
