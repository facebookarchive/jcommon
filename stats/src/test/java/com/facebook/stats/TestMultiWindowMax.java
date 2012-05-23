package com.facebook.stats;

import org.joda.time.DateTimeUtils;
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
