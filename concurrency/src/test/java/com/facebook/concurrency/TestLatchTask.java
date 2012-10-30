package com.facebook.concurrency;

import com.facebook.testing.TestUtils;
import com.facebook.testing.ThreadHelper;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicBoolean;

public class TestLatchTask  {
  private LatchTask task;
  private ThreadHelper threadHelper;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    task = new LatchTask();
    threadHelper = new ThreadHelper();
  }
  
  @Test(groups = "fast")
  public void testSanity() throws Exception {
    final AtomicBoolean completed = new AtomicBoolean(false);
    Thread thread = threadHelper.doInThread(
      new Runnable() {
        @Override
        public void run() {
          try {
            task.await();
            completed.set(true);
          } catch (InterruptedException e) {
            Assert.fail("exception", e);
            throw new RuntimeException(e);
          }
        }
      }
    );
    
    // imperfect way to test for lock, but has to suffice
    TestUtils.waitUntilThreadBlocks(thread);
    // trigger latch
    task.run();
    // wait for thread to complete
    thread.join();
    Assert.assertTrue(completed.get(), "blocked task did not complete normally");
  }
}
