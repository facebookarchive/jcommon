package com.facebook.stats;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
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
