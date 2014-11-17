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

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class TestRobustProxy {
  private Fuu fuuProxy;
  private Fuu nullFuu;
  private AtomicInteger called;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    called = new AtomicInteger(0);
    fuuProxy = RobustProxy.wrap(
      Fuu.class, new Fuu() {
        @Override
        public void bar() {
          called.incrementAndGet();
        }

        @Override
        public int getInt() {
          return 1601;
        }
      }
    );
    nullFuu = RobustProxy.wrap(Fuu.class, null);
  }

  @Test(groups = {"fast", "local"})
  public void testValidProxy() throws Exception {
    fuuProxy.bar();
    Assert.assertEquals(called.get(), 1);
    Assert.assertEquals(fuuProxy.getInt(), 1601);
  }

  @Test(groups = {"fast", "local"}, expectedExceptions = {UnsupportedOperationException.class})
  public void testNullProxy() throws Exception {
    nullFuu.bar();
    Assert.assertEquals(called.get(), 1);
    nullFuu.getInt();
    Assert.fail("expected error");
  }

  private interface Fuu {
    void bar();
    int getInt();
  }
}
