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

import com.facebook.util.TimeUtil;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

public class TestCompositeMin {

  @Test(groups = "fast")
  public void testSanity() throws Exception {
    TimeUtil.setNow(new DateTime("2012-01-01", DateTimeZone.UTC));

    CompositeMin compositeMax = new CompositeMin(
        Duration.standardSeconds(30), Duration.standardSeconds(10)
      );

    compositeMax.add(2);
    compositeMax.add(3);
    Assert.assertEquals(compositeMax.getValue(), 2);
    TimeUtil.advanceNow(Duration.standardSeconds(10));
    compositeMax.add(1);
    Assert.assertEquals(compositeMax.getValue(), 1);
    TimeUtil.advanceNow(Duration.standardSeconds(10));
    compositeMax.add(6);
    Assert.assertEquals(compositeMax.getValue(), 1);
    TimeUtil.advanceNow(Duration.standardSeconds(30));
    Assert.assertEquals(compositeMax.getValue(), 6);
  }

  @AfterMethod(alwaysRun = true)
  public void tearDown() throws Exception {
    DateTimeUtils.setCurrentMillisSystem();
  }
}
