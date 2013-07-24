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
package com.facebook.stats;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestEventRate {
  private EventRate rate;
  private CompositeSum eventCounter;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    DateTimeUtils.setCurrentMillisFixed(new DateTime("2010-01-01").getMillis());
    
    eventCounter = new CompositeSum(Duration.standardSeconds(60)); 
    rate = new EventRateImpl(eventCounter, Duration.standardSeconds(60));
  }

  @Test(groups = "fast")
  public void testSanity1() throws Exception {
    rate.add(300);
    advanceNowSeconds(30);
    Assert.assertEquals(rate.getValue(), 10);
    advanceNowSeconds(30);
    Assert.assertEquals(rate.getValue(), 5);
    advanceNowSeconds(6);
    Assert.assertEquals(rate.getValue(), 0);
    rate.add(60);
    Assert.assertEquals(rate.getValue(), 1);
  }

  private void advanceNowSeconds(int seconds) {
    DateTimeUtils.setCurrentMillisFixed(
      DateTimeUtils.currentTimeMillis() + seconds * 1000
    );
  }
}
