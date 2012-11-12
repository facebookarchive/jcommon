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

import com.facebook.collectionsbase.Lists;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

public class TestPackedByteArray {
  private byte[][] original;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    // NOTE: since 0,1 are special characters the byte values MUST be > 1
    // in practice, we require ascii printable characters
    original = new byte[][]{
      new byte[]{2, 2, 3},
      new byte[]{2, 3, 2},
      new byte[]{3, 2},
      new byte[]{10, 100, 50},
      "a marginally long string this is".getBytes("UTF-8"),
      new byte[]{2, 3},
      new byte[]{3}
    };
  }
  
  @Test(groups = "fast")
  public void testSanity() throws Exception {
    byte[] packed = PackedByteArray.pack(
      original[0],
      original[1],
      original[2],
      original[3],
      original[4],
      original[5],
      original[6]
    );
    byte[][] unpacked = PackedByteArray.unpack(packed);

    for (int i = 0; i < original.length; i++) {
      byte[] element = PackedByteArray.getElement(packed, i);
      Assert.assertTrue(Arrays.equals(element, original[i]));
      Assert.assertTrue(Arrays.equals(original[i], unpacked[i]));
    }
  }

  @Test(groups = "fast")
  public void testCompare1() throws Exception {
    // {2, 3, 3}
    byte[] bytes1 = PackedByteArray.packComparable(
      original[0]
    );
    // {2, 3, 2}
    byte[] bytes2 = PackedByteArray.packComparable(
      original[1]
    );
    // => -1
    Assert.assertEquals(Lists.compareArrays(bytes1, bytes2), -1);
  }

  @Test(groups = "fast")
  public void testCompare2() throws Exception {
    // { {0, 0, 1} }
    byte[] bytes1 = PackedByteArray.packComparable(
      original[1]
    );
    // {{0, 1}, {0} }
    byte[] bytes2 = PackedByteArray.packComparable(
      original[5], original[6]
    );
    // => 1
    Assert.assertEquals(Lists.compareArrays(bytes1, bytes2), 1);
  }

  @Test(groups = "fast")
  public void testConversions() throws Exception {
    byte[] packed = PackedByteArray.pack(
      original[0],
      original[1],
      original[2],
      original[3],
      original[4],
      original[5],
      original[6]
    );
    byte[][] unpacked = PackedByteArray.unpack(packed);
    byte[] comparablePacked = PackedByteArray.packComparable(unpacked);

    List<byte[]> bytesList = PackedByteArray.unpackComparable(comparablePacked);

    for (int i = 0; i < unpacked.length; i++) {
      Assert.assertEquals(bytesList.get(i), unpacked[i]);
    }
  }
}
