package com.facebook.config.dynamic;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class TestOptionImpl {
  private Option<String> option;

  @BeforeMethod(alwaysRun = true)
  protected void setUp() throws Exception {
    option = new OptionImpl<String>();
  }

  @Test(groups = "fast")
  public void testGetValueUninitialized() throws Exception {
    Assert.assertNull(option.getValue());
  }

  @Test(groups = "fast")
  public void testGetValue() throws Exception {
    option.setValue("testing");
    Assert.assertEquals(option.getValue(), "testing");
  }

  @Test(groups = "fast")
  public void testWatcher() throws Exception {
    Watcher watcher = new Watcher();

    option.addWatcher(watcher);
    option.setValue("test");
    Assert.assertEquals(option.getValue(), "test");
    watcher.assertState("test", 1);

    option.setValue("another test");
    Assert.assertEquals(option.getValue(), "another test");
    watcher.assertState("another test", 2);

    option.removeWatcher(watcher);
    option.setValue("penultimate test");
    Assert.assertEquals(option.getValue(), "penultimate test");
    watcher.assertState("another test", 2);

    option.addWatcher(watcher);
    option.addWatcher(watcher);
    option.addWatcher(watcher);
    option.setValue("final test");
    Assert.assertEquals(option.getValue(), "final test");
    watcher.assertState("final test", 3);
  }

  @Test(groups = "fast")
  public void testWatcherException() throws Exception {
    Watcher watcher1 = new Watcher();
    Watcher watcher2 = new Watcher();
    final AtomicInteger failureCount = new AtomicInteger();
    OptionWatcher<String> failure = new OptionWatcher<String>() {
      @Override
      public void propertyUpdated(String value) throws Exception {
        failureCount.incrementAndGet();
        throw new Exception("fail!");
      }
    };

    option.addWatcher(watcher1);
    option.addWatcher(failure);
    option.addWatcher(watcher2);
    option.setValue("test");
    Assert.assertEquals(option.getValue(), "test");
    watcher1.assertState("test", 1);
    watcher2.assertState("test", 1);
    Assert.assertEquals(failureCount.get(), 1);
  }

  private static class Watcher implements OptionWatcher<String> {
    private String value;
    private int updatedCount;

    @Override
    public void propertyUpdated(String value) throws Exception {
      this.value = value;
      ++updatedCount;
    }

    public void assertState(String expectedValue, int expectedUpdateCount) {
      Assert.assertEquals(value, expectedValue);
      Assert.assertEquals(updatedCount, expectedUpdateCount);
    }
  }
}
