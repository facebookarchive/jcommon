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
package com.facebook.lifecycle;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.EnumMap;

public class TestShutdownManager {
  private int position = 0;
  private EnumMap<TheStages, Integer> order;
  private ShutdownManager<TheStages> shutdownManager;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    order = new EnumMap<TheStages, Integer>(TheStages.class);
    shutdownManager = new ShutdownManagerImpl<TheStages>(TheStages.class, TheStages.DEFAULT);
    
    for (TheStages stage : TheStages.values()) {
      shutdownManager.addShutdownHook(new ShutdownHook(stage));
    }
  }
  
  @Test(groups = "fast")
  public void testOrder() throws Exception {
  	shutdownManager.shutdown();
    Assert.assertEquals(order.get(TheStages.BEFORE).longValue(), 0L);
    Assert.assertEquals(order.get(TheStages.ONE).longValue(), 1L);
    Assert.assertEquals(order.get(TheStages.TWO).longValue(), 2L);
    Assert.assertEquals(order.get(TheStages.THREE).longValue(), 3L);
    Assert.assertEquals(order.get(TheStages.DEFAULT).longValue(), 4L);
    Assert.assertEquals(order.get(TheStages.AFTER).longValue(), 5L);
  }
  
  private class ShutdownHook implements Runnable {
    private final TheStages stage;

    private ShutdownHook(TheStages stage) {
      this.stage = stage;
    }

    @Override
    public void run() {
      order.put(stage, position++);
    }
  }
  
  private static enum TheStages {
    BEFORE,
    ONE,
    TWO,
    THREE,
    DEFAULT,
    AFTER,
    ;
  }
}