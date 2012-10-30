package com.facebook.config.dynamic;

import com.google.common.base.Function;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class TestOptionTranslator {
  private Option<String> stringOption;
  private Option<Integer> integerOption;

  @BeforeMethod(alwaysRun = true)
  protected void setUp() throws Exception {
    Function<String, Integer> translator = new Function<String, Integer>() {
      @Override
      public Integer apply(String input) {
        return input == null ? -1 : Integer.parseInt(input);
      }
    };

    stringOption = new OptionImpl<>();
    integerOption = new OptionTranslator<>(stringOption, translator);
  }

  @Test(groups = "fast")
  public void testGetValueUninitialized() throws Exception {
    Assert.assertEquals(integerOption.getValue().intValue(), -1);
  }

  @Test(groups = "fast")
  public void testGetValue() throws Exception {
    stringOption.setValue("123");
    Assert.assertEquals(integerOption.getValue().intValue(), 123);
  }

  @Test(groups = "fast")
  public void testWatcher() throws Exception {
    final AtomicInteger integerValue = new AtomicInteger();
    final AtomicInteger updatedCount = new AtomicInteger();
    OptionWatcher<Integer> watcher = new OptionWatcher<Integer>() {
      @Override
      public void propertyUpdated(Integer value) throws Exception {
        integerValue.set(value);
        updatedCount.incrementAndGet();
      }
    };

    integerOption.addWatcher(watcher);
    stringOption.setValue("123");
    Assert.assertEquals(integerOption.getValue().intValue(), 123);
    Assert.assertEquals(integerValue.get(), 123);
    Assert.assertEquals(updatedCount.get(), 1);

    stringOption.setValue("456");
    Assert.assertEquals(integerOption.getValue().intValue(), 456);
    Assert.assertEquals(integerValue.get(), 456);
    Assert.assertEquals(updatedCount.get(), 2);

    integerOption.removeWatcher(watcher);
    stringOption.setValue("789");
    Assert.assertEquals(integerOption.getValue().intValue(), 789);
    Assert.assertEquals(integerValue.get(), 456);
    Assert.assertEquals(updatedCount.get(), 2);

    integerOption.addWatcher(watcher);
    integerOption.addWatcher(watcher);
    integerOption.addWatcher(watcher);
    stringOption.setValue("101112");
    Assert.assertEquals(integerOption.getValue().intValue(), 101112);
    Assert.assertEquals(integerValue.get(), 101112);
    Assert.assertEquals(updatedCount.get(), 3);
  }
}
