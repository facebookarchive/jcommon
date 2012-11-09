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

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.concurrent.atomic.AtomicBoolean;

public class TestMultiWindowRate {
  private static final Logger LOG = Logger.getLogger(TestMultiWindowRate.class);
  
  private DateTime now;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    now = new DateTime("2010-01-01T00:00:00");
    DateTimeUtils.setCurrentMillisFixed(now.getMillis());
  }

  /**
   * This test always succeeds, but it will show you the rate at which a counter
   * can be called on your machine when you build this 
   */
  @Test(groups = "fast")
  public void testPerformance() throws Exception {
    NumberFormat format = new DecimalFormat();
    long i = 0;
    long start = System.nanoTime();
    long end;
    
    while (i < 20000000) {
      i++;
    }

    end = System.nanoTime();

    LOG.info("ceiling rate/s : " + format.format(1000000000 * i/ (end - start)));
    
    final MultiWindowRate rate = new MultiWindowRate(500);
    final AtomicBoolean done = new AtomicBoolean(false);
    Thread t = new Thread(new Runnable() {
      @Override
      public void run() {
        while (!done.get()) {
          rate.add(1);
        }
      }
    });
    
    start = System.nanoTime();
    end = start;
    t.start();
    try {
      Thread.sleep(3250);
      done.set(true);
      end = System.nanoTime();
      t.join();
    } catch (InterruptedException e) {
      LOG.error("interrupted");
    }

    long elapsedNanos = end - start;

    LOG.info(
      "rate/s : " + format.format(1000000000 * rate.getAllTimeSum() / elapsedNanos)
    );
  }
  
  @Test(groups = "fast")
  public void testRates() throws Exception {
  	MultiWindowRate rate = new MultiWindowRate();
    long initialValue = 60000;
    long rate30Seconds = initialValue/ 30;
    long rate60Seconds = initialValue / 60;
    long rate2Minutes = initialValue / 120;
    long rate11Minutes = initialValue / 660;
    long rate60Minutes = initialValue / 3600;
    long rate120Minutes = initialValue / 7200;
    assertRateValues(rate, 0, 0, 0, 0);
    advanceNowSeconds(30);
    rate.add(initialValue); // rate for 30s period
    assertRateValues(rate, rate30Seconds, rate30Seconds, rate30Seconds, rate30Seconds);
    advanceNowSeconds(30); // rate for 60s period
    assertRateValues(rate, rate60Seconds, rate60Seconds, rate60Seconds, rate60Seconds);
    advanceNowMinutes(1); // 1-minute loses value
    assertRateValues(rate, 0, rate2Minutes, rate2Minutes, rate2Minutes);
    advanceNowMinutes(9); // 10-minute loses a value (total = 11m)
    assertRateValues(rate, 0, 0, rate11Minutes, rate11Minutes);
    advanceNowMinutes(49); // total= 60m
    assertRateValues(rate, 0, 0, rate60Minutes, rate60Minutes);
    advanceNowMinutes(60); // total= 120m
    assertRateValues(rate, 0, 0, 0, rate120Minutes);
    rate.add(initialValue);
    
    // we have sent 2 * initialValue 
    assertRateValues(
      rate,
      initialValue / 60,
      initialValue / 600,
      initialValue / 3600,
      2 * initialValue / 7200
    );
  }
  
  @Test(groups = "fast")
  public void testMerge() throws Exception {
    MultiWindowRate rate1 = new MultiWindowRate();
    MultiWindowRate rate2 = new MultiWindowRate();
  	
    rate1.add(1);
    rate2.add(2);
    
    Assert.assertEquals(rate1.merge(rate2).getAllTimeSum(), 3);
    Assert.assertEquals(rate1.merge(rate2).getHourSum(), 3);
    Assert.assertEquals(rate1.merge(rate2).getTenMinuteSum(), 3);
    Assert.assertEquals(rate1.merge(rate2).getMinuteSum(), 3);
  }
  
  @Test(groups = "fast")
  public void testMergeWithZero() throws Exception {
    MultiWindowRate rate1 = new MultiWindowRate();
    MultiWindowRate rate2 = new MultiWindowRate();
  	
    rate2.add(2);
    Assert.assertEquals(rate1.merge(rate2).getAllTimeSum(), 2);
    Assert.assertEquals(rate1.merge(rate2).getHourSum(), 2);
    Assert.assertEquals(rate1.merge(rate2).getTenMinuteSum(), 2);
    Assert.assertEquals(rate1.merge(rate2).getMinuteSum(), 2);  	
  }

  private void assertRateValues(
    MultiWindowRate rate, long minute, long tenMinute, long hour, long allTime
  ) {
    Assert.assertEquals(rate.getMinuteRate(), minute);
    Assert.assertEquals(rate.getTenMinuteRate(), tenMinute);
    Assert.assertEquals(rate.getHourRate(), hour);
    Assert.assertEquals(rate.getAllTimeRate(), allTime);
  }
  
  private void advanceNowMinutes(int minutes) {
    advanceNowSeconds(minutes * 60);    
  }

  private void advanceNowSeconds(int seconds) {
    DateTime updatedNow = new DateTime(now.plusSeconds(seconds));
    DateTimeUtils.setCurrentMillisFixed(updatedNow.getMillis());
    now = updatedNow;
  }
}