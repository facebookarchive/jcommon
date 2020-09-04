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
package com.facebook.collections.specialized;

import com.facebook.collections.ConcurrentSetMap;
import java.util.Set;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestTrackableSetMap {
  private TrackableSetMap<Integer, Integer, Set<Integer>> trackableSetMap;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    trackableSetMap = new TrackableSetMap<>(new ConcurrentSetMap<>());
  }

  @Test(groups = "fast")
  public void testSanity() throws Exception {
    Assert.assertEquals(trackableSetMap.hasChanged(), false);
    trackableSetMap.add(1, 1);
    Assert.assertEquals(trackableSetMap.hasChanged(), true);
    Assert.assertEquals(trackableSetMap.hasChanged(), false);
    trackableSetMap.add(1, 2);
    trackableSetMap.add(2, 3);
    trackableSetMap.add(3, 4);
    Assert.assertEquals(trackableSetMap.hasChanged(), true);
    Assert.assertEquals(trackableSetMap.hasChanged(), false);
  }
}
