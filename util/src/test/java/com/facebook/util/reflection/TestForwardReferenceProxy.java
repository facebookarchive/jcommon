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
package com.facebook.util.reflection;

import com.google.common.base.Function;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.annotation.Nullable;

public class TestForwardReferenceProxy {

  private ForwardReferenceProxy<TestFunction> proxy;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    proxy = new ForwardReferenceProxy<>(TestFunction.class);
  }

  @Test
  public void testBasic() throws Exception {
    TestFunction testFunction = proxy.setInstance(
      new TestFunction() {
        @Nullable
        @Override
        public Integer apply(String input) {
          return Integer.parseInt(input);
        }
      }
    ).get();

    int result = testFunction.apply("123");

    Assert.assertEquals(result, 123);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void testInstanceNotSet() throws Exception {
    TestFunction testFunction = proxy.get();
    testFunction.apply("don't care");
  }

  @Test
  public void testPartialProxy() throws Exception {
    TestFunction testFunction = proxy.get();

    Assert.assertNotNull(testFunction);
  }

  private interface TestFunction extends Function<String, Integer> {
    @Override
    Integer apply(@Nullable String input);
  }
}
