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
package com.facebook.data.types;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestIntegerDatum {
  private IntegerDatum booleanValue;
  private IntegerDatum byteValue;
  private IntegerDatum shortValue;
  private IntegerDatum intValue1;
  private IntegerDatum intValue2;
  private IntegerDatum intValue3;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    booleanValue = new IntegerDatum(1);
    byteValue = new IntegerDatum(100);
    shortValue = new IntegerDatum(1000);
    intValue1 = new IntegerDatum(1000000);
    intValue2 = new IntegerDatum(1000000);
    intValue3 = new IntegerDatum(2000000);
  }

  @Test(groups = "fast")
  public void testSanity() throws Exception {
    Assert.assertEquals(booleanValue.asBoolean(), true);
    Assert.assertEquals(byteValue.asByte(), 100);
    Assert.assertEquals(shortValue.asShort(), 1000);
    Assert.assertEquals(intValue1.asInteger(), 1000000);
    Assert.assertEquals(intValue1.asLong(), 1000000L);
  }

  @Test(groups = "fast")
  public void testCompare() throws Exception {
    Assert.assertEquals(intValue1.compareTo(intValue1), 0);
    Assert.assertEquals(intValue1.compareTo(intValue2), 0);
    Assert.assertEquals(intValue1.compareTo(intValue3), -1);
    Assert.assertEquals(intValue3.compareTo(intValue1), 1);
  }

  @Test(groups = "fast")
  public void testEquals() throws Exception {
    Assert.assertEquals(intValue1, intValue1);
    Assert.assertEquals(intValue1, intValue2);
    Assert.assertFalse(intValue1.equals(intValue3));
    Assert.assertEquals(
      DatumFactory.toDatum(true),
      DatumFactory.toDatum(1)
    );
    Assert.assertEquals(
      DatumFactory.toDatum((byte) 100),
      DatumFactory.toDatum(100)
    );
    Assert.assertEquals(
      DatumFactory.toDatum((short) 100),
      DatumFactory.toDatum(100)
    );
    Assert.assertEquals(
      DatumFactory.toDatum((float) 100),
      DatumFactory.toDatum(100)
    );
    Assert.assertEquals(
      DatumFactory.toDatum((double) 100),
      DatumFactory.toDatum(100)
    );
    Assert.assertEquals(
      DatumFactory.toDatum("100"),
      DatumFactory.toDatum(100)
    );
  }
}
