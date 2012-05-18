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
    DateTimeUtils.setCurrentMillisFixed(
      Duration.standardMinutes(5).getMillis()
    );
    Assert.assertEquals(multiWindowMin.getMinuteValue(), Long.MAX_VALUE);
    Assert.assertEquals(multiWindowMin.getTenMinuteValue(), 3);
    Assert.assertEquals(multiWindowMin.getHourValue(), 3);
    Assert.assertEquals(multiWindowMin.getAllTimeValue(), 3);

    // Clear 10 minute window
    DateTimeUtils.setCurrentMillisFixed(
      Duration.standardMinutes(15).getMillis()
    );
    Assert.assertEquals(multiWindowMin.getMinuteValue(), Long.MAX_VALUE);
    Assert.assertEquals(multiWindowMin.getTenMinuteValue(), Long.MAX_VALUE);
    Assert.assertEquals(multiWindowMin.getHourValue(), 3);
    Assert.assertEquals(multiWindowMin.getAllTimeValue(), 3);

    // Clear hour window window
    DateTimeUtils.setCurrentMillisFixed(
      Duration.standardHours(2).getMillis()
    );
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
