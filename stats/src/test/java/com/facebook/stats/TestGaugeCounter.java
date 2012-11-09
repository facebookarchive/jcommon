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
