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

import com.facebook.data.types.BooleanDatum;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestBooleanDatum {
  private BooleanDatum trueBooleanDatum1;
  private BooleanDatum trueBooleanDatum2;
  private BooleanDatum falseBooleanDatum;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    trueBooleanDatum1 = new BooleanDatum(true);
    trueBooleanDatum2 = new BooleanDatum(true);
    falseBooleanDatum = new BooleanDatum(false);
  }

  @Test(groups = "fast")
  public void testSanity() throws Exception {
    Assert.assertEquals(trueBooleanDatum1.asInteger(), 1);
    Assert.assertEquals(falseBooleanDatum.asInteger(), 0);
  }

  @Test(groups = "fast")
  public void testCompare() throws Exception {
    Assert.assertEquals(trueBooleanDatum1.compareTo(trueBooleanDatum1), 0);
    Assert.assertEquals(trueBooleanDatum1.compareTo(trueBooleanDatum2), 0);
    Assert.assertEquals(trueBooleanDatum1.compareTo(falseBooleanDatum), 1);
    Assert.assertEquals(falseBooleanDatum.compareTo(trueBooleanDatum2), -1);
  }

  @Test(groups = "fast")
  public void testEquals() throws Exception {
    Assert.assertEquals(trueBooleanDatum1, trueBooleanDatum1);
    Assert.assertEquals(trueBooleanDatum1, trueBooleanDatum2);
    Assert.assertFalse(trueBooleanDatum1.equals(falseBooleanDatum));
  }
}
