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

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestStringOptions {
  private StringOptions options;
  private Option<String> option1;
  private Option<String> option2;

  @BeforeMethod(alwaysRun = true)
  protected void setUp() throws Exception {
    options = new StringOptions();
    option1 = options.getOption("test_option");
    option2 = options.getOption("test_option");
  }

  @Test(groups = "fast")
  public void testSameOptionUpdates() throws Exception {
    Assert.assertNull(option1.getValue());
    Assert.assertNull(option2.getValue());
    option1.setValue("foofoo");
    Assert.assertEquals(option1.getValue(), "foofoo");
    Assert.assertEquals(option2.getValue(), "foofoo");
  }

  @Test(groups = "fast")
  public void testSetOptionPropgates() throws Exception {
    Assert.assertNull(option1.getValue());
    Assert.assertNull(option2.getValue());
    options.setOption("test_option", "foofoo");
    Assert.assertEquals(option1.getValue(), "foofoo");
    Assert.assertEquals(option2.getValue(), "foofoo");
  }
}
