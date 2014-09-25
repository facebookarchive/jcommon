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
