package com.facebook.concurrency;

import com.facebook.testing.TestUtils;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.CountDownLatch;

public class TestCriticalSectionFactory {
  private static final Logger LOG = Logger.getLogger(TestCriticalSectionFactory.class);

  private int count;
  private CountDownLatch criticalSectionLatch;
  private CountDownLatch entryLatch;
  private Runnable runnable;
  private CriticalSectionFactory factory;

  @BeforeMethod(alwaysRun = true)
  private void setup() {
    count = 0;
    criticalSectionLatch = new CountDownLatch(1);
    entryLatch = new CountDownLatch(1);
    runnable = new Runnable() {
      @Override
      public void run() {
        try {
          entryLatch.countDown();
          criticalSectionLatch.await();
        } catch (InterruptedException e) {
          LOG.error("test interrupted");
          Assert.fail("test interrupted");
        }
        
        count++;
      }
    };
    factory = new CriticalSectionFactory();
  }

  @Test(groups = "fast")
  public void testCriticalSectionFactory() throws InterruptedException {
    final Runnable criticalSection = factory.wrap(runnable);

    // t1 will stop in the critical section
    Thread t1 = TestUtils.runInThread(criticalSection);
    entryLatch.await();
    // now start t2 and wait until it skips critical section
    Thread t2 = TestUtils.runInThread(criticalSection);
    t2.join();
    // let t1 out of critical section 
    criticalSectionLatch.countDown();
    t1.join();
    
    // only t1 should make it through critical section
    Assert.assertEquals(count, 1, "too many critical section calls");
  }
}
