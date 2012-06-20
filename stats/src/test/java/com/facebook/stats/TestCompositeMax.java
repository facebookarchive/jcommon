package com.facebook.stats;

import com.facebook.util.TimeUtil;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

public class TestCompositeMax {

  @Test(groups = "fast")
  public void testSanity() throws Exception {
    TimeUtil.setNow(new DateTime("2012-01-01", DateTimeZone.UTC));
    CompositeMax compositeMax = new CompositeMax(
        Duration.standardSeconds(30), Duration.standardSeconds(10)
      );

    compositeMax.add(2);
    compositeMax.add(3);
    Assert.assertEquals(compositeMax.getValue(), 3);

    // Advance to next bucket
    TimeUtil.advanceNow(Duration.standardSeconds(10));
    compositeMax.add(5);
    Assert.assertEquals(compositeMax.getValue(), 5);

    // Advance to last bucket
    TimeUtil.advanceNow(Duration.standardSeconds(10));
    compositeMax.add(1);
    Assert.assertEquals(compositeMax.getValue(), 5);

    // Drain all but last bucket
    TimeUtil.advanceNow(Duration.standardSeconds(30));
    Assert.assertEquals(compositeMax.getValue(), 1);
  }

  @AfterMethod(alwaysRun = true)
  public void tearDown() throws Exception {
    DateTimeUtils.setCurrentMillisSystem();
  }
}
