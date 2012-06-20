package com.facebook.stats;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.joda.time.Duration;
import org.joda.time.ReadableDateTime;
import org.joda.time.ReadableDuration;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestCompositeSum {
  private DateTime base;
  private DateTime now;
  private EventCounter baseCounter1;
  private EventCounter baseCounter2;
  private EventCounter baseCounter3;
  private EventCounter baseCounter4;
  private EventCounter baseCounter5;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    base = new DateTime("2010-01-01T00:00:00");
    now = new DateTime(base);
    baseCounter1 = nextEventWindow(1);
    baseCounter1.add(1);
    baseCounter2 = nextEventWindow(1);
    baseCounter2.add(2);
    baseCounter3 = nextEventWindow(1);
    baseCounter3.add(4);
    baseCounter4 = nextEventWindow(1);
    baseCounter4.add(8);
    baseCounter5 = nextEventWindow(1);
    baseCounter5.add(16);
    setNow(now);
  }

  @Test(groups = "fast")
  public void testFallOff() throws Exception {
    EventCounter firstCounter = nextEventWindow(1);
    CompositeEventCounterIf<EventCounter> counter =
      newCompositeEventCounter(Duration.standardMinutes(3))
        .addEventCounter(firstCounter);

    // single element set
    counter.add(100);
    Assert.assertEquals(firstCounter.getValue(), counter.getValue());
    EventCounter middleCounter = nextEventWindow(2);
    counter.addEventCounter(middleCounter);

    // now has two elements
    middleCounter.add(1000);
    setNow(middleCounter.getEnd());
    Assert.assertEquals(counter.getValue(), 1100);
    // add one more
    EventCounter lastCounter = nextEventWindow(1);
    counter.addEventCounter(lastCounter);
    setNow(lastCounter.getEnd().toDateTime().minusMillis(1));
    DateTimeUtils.setCurrentMillisFixed(now.getMillis());
    Assert.assertEquals(counter.getValue(), 1001);
    // test updating last counter
    lastCounter.add(200);
    Assert.assertEquals(counter.getValue(), 1201);
    counter.add(5);
    Assert.assertEquals(lastCounter.getValue(), 205);
    Assert.assertEquals(counter.getValue(), 1206);

  }

  @Test(groups = "fast")
  public void testNestedComposite1() throws Exception {
    CompositeEventCounterIf<EventCounter> counter1 =
      newCompositeEventCounter(Duration.standardMinutes(3))
        .addEventCounter(baseCounter2)
        .addEventCounter(baseCounter3);
    CompositeEventCounterIf<EventCounter> counter2 =
      newCompositeEventCounter(Duration.standardMinutes(3))
        .addEventCounter(baseCounter1)
        .addEventCounter(
          (EventCounter) counter1
        );

    // all 3 counters are in counter1's range
    setNow(baseCounter3.getEnd());
    Assert.assertEquals(counter2.getValue(), 4 + 2 + 1);

    // this will push baseCounter1 off
    counter1.addEventCounter(baseCounter4);
    setNow(baseCounter4.getEnd());
    Assert.assertEquals(counter2.getValue(), 8 + 4 + 2);

    // this should push counter1 out of counter2
    counter2.addEventCounter(baseCounter5);
    setNow(baseCounter5.getEnd());
    Assert.assertEquals(counter2.getValue(), 16 + 8 + 4);
  }

  @Test(groups = "fast")
  public void testNestedComposite2() throws Exception {
    CompositeEventCounterIf<EventCounter> counter1 =
      newCompositeEventCounter(Duration.standardMinutes(2))
        .addEventCounter(baseCounter2)
        .addEventCounter(baseCounter3);
    CompositeEventCounterIf<EventCounter> counter2 =
      newCompositeEventCounter(Duration.standardMinutes(3))
        .addEventCounter(baseCounter1)
        .addEventCounter(
          (EventCounter) counter1
        );

    // all 3 counters are in counter1's range
    setNow(baseCounter3.getEnd());    
    Assert.assertEquals(counter1.getValue(), 4 + 2);
    Assert.assertEquals(counter2.getValue(), 4 + 2 + 1);

    // we lose baseCounter2
    counter2.addEventCounter(baseCounter4);
    setNow(baseCounter4.getEnd());    
    Assert.assertEquals(counter1.getValue(), 4);
    Assert.assertEquals(counter2.getValue(), 8 + 4);

    // counter 1 is empty
    counter2.addEventCounter(baseCounter5);
    setNow(baseCounter5.getEnd());    
    Assert.assertEquals(counter1.getValue(), 0);
    Assert.assertEquals(counter2.getValue(), 16 + 8);
  }

  @Test(groups = "fast")
  public void testSingleWindow() throws Exception {
    EventCounter baseCounter = nextEventWindow(1);
    baseCounter.add(100);
    CompositeEventCounterIf<EventCounter> counter =
      newCompositeEventCounter(new Duration(3 * 60000))
        .addEventCounter(baseCounter);

    Assert.assertEquals(baseCounter.getValue(), counter.getValue());
    baseCounter.add(200);
    Assert.assertEquals(baseCounter.getValue(), counter.getValue());
    counter.add(100);
    Assert.assertEquals(baseCounter.getValue(), counter.getValue());
  }

  @Test(groups = "fast")
  public void testCollapseSanity() throws Exception {
    CompositeSum counter =
      newCompositeEventCounter(Duration.standardMinutes(60));

    DateTime start = new DateTime(base.getMillis());
    for (int i = 0; i < 60; i++) {
      EventCounter eventCounter = nextEventWindow(1);
      eventCounter.add(1);
      counter.addEventCounter(eventCounter);
    }

    setNow(base);
    Assert.assertEquals(counter.getValue(), 60);
    Assert.assertEquals(counter.getStart(), start);
    Assert.assertEquals(counter.getEnd(), start.plusMinutes(60));
  }

  @Test(groups = "fast")
  public void testMergeComposite() throws Exception {
    setNow(base);
    DateTime start = base;

    CompositeSum counter1 =
      newCompositeEventCounter(Duration.standardMinutes(60));
    CompositeSum counter2 =
      newCompositeEventCounter(Duration.standardMinutes(60));

    counter1.add(100);
    counter2.add(10);
    advanceNowMinutes(6);
    counter1.addEventCounter(windowMinutes(6));
    counter2.addEventCounter(windowMinutes(6));
    counter1.add(100);
    counter2.add(10);
    advanceNowMinutes(6);
    counter1.addEventCounter(windowMinutes(6));
    counter2.addEventCounter(windowMinutes(6));
    counter1.add(100);
    counter2.add(10);
    EventCounter counter3 = counter1.merge(counter2);
    Assert.assertEquals(counter3.getStart(), start);
    Assert.assertEquals(
      counter3.getEnd(), now.plusMinutes(6)
    );
    Assert.assertEquals(counter3.getValue(), 330);
  }

  @Test(groups = "fast")
  public void testInterleavedMerge() throws Exception {
    setNow(base);

    CompositeSum counter1 =
      newCompositeEventCounter(Duration.standardMinutes(60));
    CompositeSum counter2 =
      newCompositeEventCounter(Duration.standardMinutes(60));

    counter2.add(1);
    advanceNowMinutes(30);
    counter1.add(10);
    advanceNowMinutes(30);
    counter2.add(100);
    advanceNowMinutes(5);
    counter1.add(1000);
    // 31 minutes => 1 hr 36m => |100|1000|10000|
    // but 10 drops off
    advanceNowMinutes(31);
    counter2.add(10000);
    
    EventCounter counter3 = counter1.merge(counter2);
    
    Assert.assertEquals(counter3.getValue(), 11100);
  }

  @Test(groups = "fast")
  public void testAutoCreateNewCounter() throws Exception {
    CompositeSum counter =
      newCompositeEventCounter(Duration.standardMinutes(10));
    // this creates a single internal counter with a value of 1 
    counter.add(1);
    Assert.assertEquals(counter.getValue(), 1);
    // advancing time and adding 1 creates a second internal counter
    advanceNowMinutes(2);
    counter.add(1);
    Assert.assertEquals(counter.getValue(), 2);
    // this will cause the getValue() to roll off the first counter
    advanceNowMinutes(9);
    Assert.assertEquals(counter.getValue(), 1);
  }
  
  @Test(groups = "fast")
  public void testPartialExpiration() throws Exception {
    setNow(base);
    CompositeSum counter = newCompositeEventCounter(Duration.standardMinutes(10));

    for (int i = 0; i < 10; i++) {
      counter.add(10);
      advanceNowMinutes(1);
    }

    Assert.assertEquals(counter.getValue(), 100);
    advanceNowSeconds(30);
    Assert.assertEquals(counter.getValue(), 95);
  }
  
  private void advanceNowSeconds(int seconds) {
    setNow(now.plusSeconds(seconds));    
  }
  
  private void advanceNowMinutes(int minutes) {
    setNow(now.plusMinutes(minutes));
  }

  private EventCounter windowMinutes(int minutes) {
    return new SumEventCounter(
      now, now.plusMinutes(minutes)
    );
  }

  private CompositeSum newCompositeEventCounter(
    ReadableDuration maxLength
  ) {
    return new CompositeSum(maxLength);
  }

  private EventCounter nextEventWindow(int minutes) {
    EventCounter counter = new SumEventCounter(base, base.plusMinutes(minutes));
    base = base.plusMinutes(minutes);

    return counter;
  }

  private void setNow(ReadableDateTime value) {
    DateTimeUtils.setCurrentMillisFixed(value.getMillis());
    now = new DateTime(value);
  }
}
