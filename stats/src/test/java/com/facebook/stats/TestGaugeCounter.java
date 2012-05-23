package com.facebook.stats;

import org.joda.time.Duration;
import org.testng.Assert;
import org.testng.annotations.Test;


public class TestGaugeCounter {

  @Test(groups = "fast")
  public void testBasic() throws Exception {
    CompositeGaugeCounter cgc =
      new CompositeGaugeCounter(Duration.standardMinutes(1));
    int num = 100;
    int incr = 2;
    for (int i=0; i<num; i++) {
      cgc.add(incr);
    }
    Assert.assertEquals(cgc.getValue(), num*incr);
    Assert.assertEquals(cgc.getSamples(), num);
    Assert.assertEquals(cgc.getAverage(), incr);
  }

  @Test(groups = "fast")
  public void testSpeed() throws Exception {
    CompositeGaugeCounter cgc =
      new CompositeGaugeCounter(Duration.standardMinutes(1));
    int num = 10000;                // check time after num iterations
    int total = 0;
    long start = System.currentTimeMillis();
    long tookms = 0;
    do {
      for (int i=0; i<num; i++) {
        cgc.add(1);
      }
      total += num;
      tookms = System.currentTimeMillis() - start;
    } while (tookms < 1000);
    float tooksecs = tookms / 1000.0F;
    System.out.println(
      String.format(
        "Took %02.2f secs, rate %02.2f calls/sec, %04.4f msecs/call\n",
        tooksecs, total / tooksecs, (float)tookms / total));
  }
}
