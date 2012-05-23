package com.facebook.stats;

import org.joda.time.DateTimeUtils;
import org.joda.time.Duration;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestCompositeMax {

  @Test(groups = "fast")
  public void testSanity() throws Exception {
    DateTimeUtils.setCurrentMillisFixed(0);
    CompositeMax compositeMax =
      new CompositeMax(
        Duration.standardSeconds(30), Duration.standardSeconds(10)
      );

    compositeMax.add(2);
    compositeMax.add(3);
    Assert.assertEquals(compositeMax.getValue(), 3);

    // Advance to next bucket
    DateTimeUtils.setCurrentMillisFixed(
      Duration.standardSeconds(15).getMillis()
    );
    compositeMax.add(5);
    Assert.assertEquals(compositeMax.getValue(), 5);

    // Advance to last bucket
    DateTimeUtils.setCurrentMillisFixed(
      Duration.standardSeconds(25).getMillis()
    );
    compositeMax.add(1);
    Assert.assertEquals(compositeMax.getValue(), 5);

    // Drain all but last bucket
    DateTimeUtils.setCurrentMillisFixed(
      Duration.standardSeconds(60).getMillis()
    );
    Assert.assertEquals(compositeMax.getValue(), 1);

    DateTimeUtils.setCurrentMillisSystem();
  }
}
