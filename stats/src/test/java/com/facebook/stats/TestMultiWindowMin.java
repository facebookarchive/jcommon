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

import org.joda.time.DateTimeUtils;
import org.joda.time.Duration;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestMultiWindowMin {

  @Test(groups = "fast")
  public void testSanity() throws Exception {
    DateTimeUtils.setCurrentMillisFixed(0);

    MultiWindowMin multiWindowMin = new MultiWindowMin();

    multiWindowMin.add(4);
    multiWindowMin.add(3);
    Assert.assertEquals(multiWindowMin.getMinuteValue(), 3);
    Assert.assertEquals(multiWindowMin.getTenMinuteValue(), 3);
    Assert.assertEquals(multiWindowMin.getHourValue(), 3);
    Assert.assertEquals(multiWindowMin.getAllTimeValue(), 3);

    // Clear 1 minute window
    DateTimeUtils.setCurrentMillisFixed(Duration.standardMinutes(5).getMillis());
    Assert.assertEquals(multiWindowMin.getMinuteValue(), Long.MAX_VALUE);
    Assert.assertEquals(multiWindowMin.getTenMinuteValue(), 3);
    Assert.assertEquals(multiWindowMin.getHourValue(), 3);
    Assert.assertEquals(multiWindowMin.getAllTimeValue(), 3);

    // Clear 10 minute window
    DateTimeUtils.setCurrentMillisFixed(Duration.standardMinutes(15).getMillis());
    Assert.assertEquals(multiWindowMin.getMinuteValue(), Long.MAX_VALUE);
    Assert.assertEquals(multiWindowMin.getTenMinuteValue(), Long.MAX_VALUE);
    Assert.assertEquals(multiWindowMin.getHourValue(), 3);
    Assert.assertEquals(multiWindowMin.getAllTimeValue(), 3);

    // Clear hour window window
    DateTimeUtils.setCurrentMillisFixed(Duration.standardHours(2).getMillis());
    Assert.assertEquals(multiWindowMin.getMinuteValue(), Long.MAX_VALUE);
    Assert.assertEquals(multiWindowMin.getTenMinuteValue(), Long.MAX_VALUE);
    Assert.assertEquals(multiWindowMin.getHourValue(), Long.MAX_VALUE);
    Assert.assertEquals(multiWindowMin.getAllTimeValue(), 3);

    // Override all values
    multiWindowMin.add(2);
    Assert.assertEquals(multiWindowMin.getMinuteValue(), 2);
    Assert.assertEquals(multiWindowMin.getTenMinuteValue(), 2);
    Assert.assertEquals(multiWindowMin.getHourValue(), 2);
    Assert.assertEquals(multiWindowMin.getAllTimeValue(), 2);

    DateTimeUtils.setCurrentMillisSystem();
  }
}
