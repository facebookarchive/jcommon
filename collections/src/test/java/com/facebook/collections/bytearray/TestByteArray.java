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

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestByteArray {
  private AbstractByteArray byteArray1;
  private AbstractByteArray nullArray;
  private AbstractByteArray byteArray2;
  private ByteArray byteArrayNumbers;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    byteArray1 = ByteArrays.wrap("string a".getBytes());
    byteArray2 = ByteArrays.wrap("string b".getBytes());
    byteArrayNumbers = ByteArrays.wrap(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
    nullArray = ByteArrays.wrap(null);
  }

  @Test(groups = "fast")
  public void testCompareTo() throws Exception {
    Assert.assertEquals(byteArray1.compareTo(byteArray2), -1);
    Assert.assertEquals(byteArray2.compareTo(byteArray1), 1);
    Assert.assertEquals(byteArray1.compareTo(byteArray1), 0);
    Assert.assertEquals(byteArray1.compareTo(null), 1);
    Assert.assertEquals(byteArray1.compareTo(nullArray), 1);
    Assert.assertEquals(nullArray.compareTo(null), 1);
    Assert.assertEquals(nullArray.compareTo(byteArray1), -1);
  }

  @Test(groups = "fast")
  public void testEquals() throws Exception {
    // symmetric two difference arrays
    Assert.assertFalse(byteArray1.equals(byteArray2));
    Assert.assertFalse(byteArray2.equals(byteArray1));
    // reflexive
    Assert.assertTrue(byteArray1.equals(byteArray1));
    // null vs null/non-null
    Assert.assertFalse(byteArray1.equals(null));
    Assert.assertFalse(nullArray.equals(null));
    Assert.assertFalse(byteArray1.equals(nullArray));
    Assert.assertFalse(nullArray.equals(byteArray1));
  }

  @Test
  public void testIterator() throws Exception {
    int sum = 0;

    for (byte b : byteArrayNumbers) {
      sum += b;
    }

    Assert.assertEquals(sum, 55);
  }

  @Test
  public void testGet() throws Exception {
    for (int i = 0; i < 10; i++) {
      Assert.assertEquals(byteArrayNumbers.getAdjusted(i), i + 1);
    }
  }

  @Test
  public void testPut() throws Exception {
    byteArrayNumbers.putAdjusted(5, (byte) 0);
    Assert.assertEquals(byteArrayNumbers.getAdjusted(5), 0);
  }

  @Test(expectedExceptions = {IndexOutOfBoundsException.class})
  public void testOutOfBounds() throws Exception {
    byteArrayNumbers.getAdjusted(10);
  }
}
