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
package com.facebook.collectionsbase;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestReferenceCourier {

  private Courier<String> hasValue;
  private Courier<String> empty1;
  private Courier<String> empty2;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    hasValue = new ReferenceCourier<>("fuu");
    empty1 = new ReferenceCourier<>(null);
    empty2 = ReferenceCourier.empty();
  }

  @Test(groups = {"fast", "local"})
  public void testHasValue() throws Exception {
    Assert.assertTrue(hasValue.isSet());
    Assert.assertEquals(hasValue.get(), "fuu");
  }

  @Test(groups = {"fast", "local"})
  public void testEmpty1IsSet() throws Exception {
    Assert.assertFalse(empty1.isSet());
  }

  @Test(groups = {"fast", "local"}, expectedExceptions = NullPointerException.class)
  public void testEmpty1Get() throws Exception {
    empty1.get();
  }

  @Test(groups = {"fast", "local"})
  public void testempty2IsSet() throws Exception {
    Assert.assertFalse(empty2.isSet());
  }

  @Test(groups = {"fast", "local"}, expectedExceptions = NullPointerException.class)
  public void testempty2Get() throws Exception {
    empty2.get();
  }
}
