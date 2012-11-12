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

public class TestNumberCachingDatum {

  private NumberCachingDatum doubleValueAsString;
  private NumberCachingDatum doubleValue;
  private NumberCachingDatum longValueAsString;
  private NumberCachingDatum longValue;
  private NumberCachingDatum intValueAsString;
  private NumberCachingDatum intValue;
  private NumberCachingDatum stringValue;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    intValue = new NumberCachingDatum(DatumFactory.toDatum(100));
    intValueAsString = new NumberCachingDatum(DatumFactory.toDatum("100"));
    longValue = new NumberCachingDatum(DatumFactory.toDatum(Long.MAX_VALUE));
    longValueAsString = new NumberCachingDatum(DatumFactory.toDatum(Long.MAX_VALUE));
    doubleValue = new NumberCachingDatum(DatumFactory.toDatum(Math.PI));
    doubleValueAsString =
      new NumberCachingDatum(DatumFactory.toDatum(String.format("%s", Math.PI)));
    stringValue = new NumberCachingDatum(DatumFactory.toDatum("smatchemo"));
  }

  @Test(groups = "fast")
  public void testIntegerLongCompatible() throws Exception {
    Assert.assertTrue(DatumType.isLongCompatible(intValue));
    Assert.assertTrue(DatumType.isLongCompatible(intValueAsString));
  }


  @Test(groups = "fast")
  public void testLongLongCompatible() throws Exception {
    Assert.assertTrue(DatumType.isLongCompatible(longValue));
    Assert.assertTrue(DatumType.isLongCompatible(longValueAsString));
  }

  @Test(groups = "fast")
  public void testDoubleLongCompatible() throws Exception {
    Assert.assertFalse(DatumType.isLongCompatible(doubleValue));
    Assert.assertFalse(DatumType.isLongCompatible(doubleValueAsString));
  }

  @Test(groups = "fast")
  public void testStringLongCompatible() throws Exception {
    Assert.assertFalse(DatumType.isLongCompatible(stringValue));
  }
}
