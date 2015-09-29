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

public class TestCounterMap {
  private CounterMap<String> counterMap;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    counterMap = new CounterMap<>();
  }

  @Test
  public void testCleanup() throws Exception {
    counterMap.addAndGet("fuu", 1);
    counterMap.addAndGet("fuu", -1);

    Assert.assertNull(counterMap.remove("fuu"));
  }

  @Test
  public void testAddAndGet() throws Exception {
    Assert.assertEquals(counterMap.addAndGet("fuu", 1), 1);
  }

  @Test
  public void testGetAndAdd() throws Exception {
    Assert.assertEquals(counterMap.getAndAdd("fuu", 1), 0);
  }

  @Test
  public void testInitialize() throws Exception {
    Assert.assertEquals(counterMap.tryInitializeCounter("fuu", 100), 100);
    Assert.assertEquals(counterMap.addAndGet("fuu", 21), 121);
  }

  @Test
  public void trySet() throws Exception {
    Assert.assertEquals(counterMap.trySetCounter("fuu", 100), 100);
    Assert.assertEquals(counterMap.addAndGet("fuu", 21), 121);
  }
}
