package com.facebook.stats;

import org.testng.Assert;
import org.testng.annotations.Test;


public class TestMultiWindowGauge {

  // @Test(groups = "fast")
  @Test
  public void testBasic() throws Exception {
    MultiWindowGauge mwg = new MultiWindowGauge();
    int num = 100000;
    int incr = 3;
    for (int i=0; i<num; i++) {
      mwg.add(incr);
    }
    Assert.assertEquals(num * incr, mwg.getAllTimeSum());
    Assert.assertEquals(num, mwg.getAllTimeSamples());
    Assert.assertEquals(incr, mwg.getAllTimeAvg());

    Assert.assertEquals(num*incr, mwg.getMinuteSum());
    Assert.assertEquals(num, mwg.getMinuteSamples());
    Assert.assertEquals(incr, mwg.getMinuteAvg());

    MultiWindowGauge merged = mwg.merge(mwg);
    Assert.assertEquals(2*num*incr, merged.getMinuteSum());
    Assert.assertEquals(2*num, merged.getMinuteSamples());
    Assert.assertEquals(incr, merged.getMinuteAvg());
  }

  // @Test(groups = "fast")
  @Test
  public void testSpeed() throws Exception {
    MultiWindowGauge mwg = new MultiWindowGauge();
    int num = 10000;                // check time after num iterations
    int total = 0;
    long start = System.currentTimeMillis();
    long tookms = 0;
    do {
      for (int i=0; i<num; i++) {
        mwg.add(3);
      }
      total += num;
      tookms = System.currentTimeMillis() - start;
    } while (tookms < 2000);

    float tooksecs = tookms / 1000.0F;
    System.out.println(
      String.format("Took %02.2f secs, rate %02.2f calls/sec, %04.4f msecs/call\n",
                    tooksecs, total / tooksecs, (float)tookms / total));
    System.out.println("        \tsum\t\tnum\t\tavg\t\trate");
    System.out.println(
      String.format("AllTime \t%d\t%d\t%d\t%d",
                    mwg.getAllTimeSum(), mwg.getAllTimeSamples(),
                    mwg.getAllTimeAvg(), mwg.getAllTimeRate()));
    System.out.println(
      String.format("Minute  \t%d\t%d\t%d\t%d",
                    mwg.getMinuteSum(), mwg.getMinuteSamples(),
                    mwg.getMinuteAvg(), mwg.getMinuteRate()));
  }
}
