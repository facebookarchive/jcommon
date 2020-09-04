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
package com.facebook.config.dynamic;

import com.google.common.base.Function;
import java.util.concurrent.atomic.AtomicInteger;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestOptionTranslator {
  private Option<String> stringOption;
  private Option<Integer> integerOption;

  @BeforeMethod(alwaysRun = true)
  protected void setUp() throws Exception {
    Function<String, Integer> translator =
        new Function<String, Integer>() {
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
    AtomicInteger integerValue = new AtomicInteger();
    AtomicInteger updatedCount = new AtomicInteger();
    OptionWatcher<Integer> watcher =
        new OptionWatcher<Integer>() {
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
