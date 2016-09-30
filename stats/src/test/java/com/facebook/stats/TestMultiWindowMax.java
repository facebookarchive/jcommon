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

import org.joda.time.Duration;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestMultiWindowMax {

  @Test(groups = "fast")
  public void testSanity() throws Exception {
    DateTimeUtils.setCurrentMillisFixed(0);

    MultiWindowMax multiWindowMax = new MultiWindowMax();

    multiWindowMax.add(2);
    multiWindowMax.add(3);
    Assert.assertEquals(multiWindowMax.getMinuteValue(), 3);
    Assert.assertEquals(multiWindowMax.getTenMinuteValue(), 3);
    Assert.assertEquals(multiWindowMax.getHourValue(), 3);
    Assert.assertEquals(multiWindowMax.getAllTimeValue(), 3);

    // Clear 1 minute window
    DateTimeUtils.setCurrentMillisFixed(
      Duration.standardMinutes(5).getMillis()
    );
    Assert.assertEquals(multiWindowMax.getMinuteValue(), Long.MIN_VALUE);
    Assert.assertEquals(multiWindowMax.getTenMinuteValue(), 3);
    Assert.assertEquals(multiWindowMax.getHourValue(), 3);
    Assert.assertEquals(multiWindowMax.getAllTimeValue(), 3);

    // Clear 10 minute window
    DateTimeUtils.setCurrentMillisFixed(
      Duration.standardMinutes(15).getMillis()
    );
    Assert.assertEquals(multiWindowMax.getMinuteValue(), Long.MIN_VALUE);
    Assert.assertEquals(multiWindowMax.getTenMinuteValue(), Long.MIN_VALUE);
    Assert.assertEquals(multiWindowMax.getHourValue(), 3);
    Assert.assertEquals(multiWindowMax.getAllTimeValue(), 3);

    // Clear hour window window
    DateTimeUtils.setCurrentMillisFixed(
      Duration.standardHours(2).getMillis()
    );
    Assert.assertEquals(multiWindowMax.getMinuteValue(), Long.MIN_VALUE);
    Assert.assertEquals(multiWindowMax.getTenMinuteValue(), Long.MIN_VALUE);
    Assert.assertEquals(multiWindowMax.getHourValue(), Long.MIN_VALUE);
    Assert.assertEquals(multiWindowMax.getAllTimeValue(), 3);

    // Override all values
    multiWindowMax.add(4);
    Assert.assertEquals(multiWindowMax.getMinuteValue(), 4);
    Assert.assertEquals(multiWindowMax.getTenMinuteValue(), 4);
    Assert.assertEquals(multiWindowMax.getHourValue(), 4);
    Assert.assertEquals(multiWindowMax.getAllTimeValue(), 4);

    DateTimeUtils.setCurrentMillisSystem();
  }
}
