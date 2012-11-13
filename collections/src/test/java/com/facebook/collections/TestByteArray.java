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

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestByteArray {
  private ByteArray byteArray1;
  private ByteArray nullArray;
  private ByteArray byteArray2;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    byteArray1 = ByteArray.wrap("string a".getBytes());
    byteArray2 = ByteArray.wrap("string b".getBytes());
    nullArray = ByteArray.wrap(null);
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
}
